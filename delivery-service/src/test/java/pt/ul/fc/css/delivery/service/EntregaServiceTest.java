package pt.ul.fc.css.delivery.service;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import pt.ul.fc.css.delivery.domain.entrega.Entrega;
import pt.ul.fc.css.delivery.domain.entrega.EstadoEntrega;
import pt.ul.fc.css.delivery.domain.entregador.Entregador;
import pt.ul.fc.css.delivery.dto.entregaDTO.AtribuirEntregadorDTO;
import pt.ul.fc.css.delivery.events.EntregaAtualizadaPublisher;
import pt.ul.fc.css.delivery.repository.EntregaRepository;
import pt.ul.fc.css.delivery.repository.EntregadorRepository;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:delivery-test;DB_CLOSE_DELAY=-1",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false"
})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class EntregaServiceTest {

    @Autowired
    private EntregaService entregaService;

    @Autowired
    private EntregadorRepository entregadorRepository;

    @Autowired
    private EntregaRepository entregaRepository;

    // Mocks the Kafka publisher so we don't need a real Kafka broker running for tests
    @MockitoBean
    private EntregaAtualizadaPublisher entregaAtualizadaPublisher;

    @BeforeEach
    void setUp() {
        entregaRepository.deleteAll();
        entregadorRepository.deleteAll();
    }   

    @Test
    void listarEntregadores_ReturnsAllEntregadores() {
        entregadorRepository.save(new Entregador("João", "joao@tasca.local", true));
        entregadorRepository.save(new Entregador("Maria", "maria@tasca.local", false));

        List<Entregador> resultados = entregaService.listarEntregadores();

        assertThat(resultados).hasSize(2);
    }

    @Test
    void atribuirEntregador_WithAvailableDriver_Success() {
        // Arrange
        Entregador entregador = entregadorRepository.save(new Entregador("Carlos", "carlos@tasca.local", true));
        Long pedidoId = 500L;
        AtribuirEntregadorDTO dto = new AtribuirEntregadorDTO(pedidoId);

        // Act
        Entrega entrega = entregaService.atribuirEntregador(dto);

        // Assert
        assertThat(entrega).isNotNull();
        assertThat(entrega.getPedidoId()).isEqualTo(pedidoId);
        assertThat(entrega.getEntregadorId()).isEqualTo(entregador.getId());
        
        // Verify driver is no longer available in the DB
        Entregador updatedDriver = entregadorRepository.findById(entregador.getId()).orElseThrow();
        assertThat(updatedDriver.isAvailable()).isFalse();

        // Verify the success event was published to Kafka
        verify(entregaAtualizadaPublisher, times(1)).publicar(any(Entrega.class), eq("ATRIBUIDO_ENTREGADOR"));
    }

    @Test
    void atribuirEntregador_WithNoAvailableDrivers_ThrowsException() {
        // Arrange
        entregadorRepository.save(new Entregador("Carlos", "carlos@tasca.local", false)); // Not available
        Long pedidoId = 501L;

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            entregaService.atribuirEntregador(pedidoId);
        });

        assertThat(exception.getMessage()).isEqualTo("Nao ha entregadores disponiveis de momento.");
    }

    @Test
    void registarRecolha_UpdatesStateAndPublishesEvent() {
        // Arrange
        Entregador entregador = entregadorRepository.save(new Entregador("Ana", "ana@tasca.local", false));
        Long pedidoId = 600L;
        Entrega entrega = entregaRepository.save(new Entrega(pedidoId, entregador.getId(), "Rua Principal"));

        // Act
        Entrega atualizada = entregaService.registarRecolha(pedidoId);

        // Assert
        assertThat(atualizada.getEstado()).isEqualTo(EstadoEntrega.A_CAMINHO);
        verify(entregaAtualizadaPublisher, times(1)).publicar(any(Entrega.class), eq("ATRIBUIDO_ENTREGADOR"));
    }

    @Test
    void registarEntregaFinal_FreesDriverAndPublishesEvent() {
        // Arrange
        Entregador entregador = entregadorRepository.save(new Entregador("Pedro", "pedro@tasca.local", false));
        Long pedidoId = 700L;
        Entrega entrega = new Entrega(pedidoId, entregador.getId(), "Rua Destino");
        entregaRepository.save(entrega);

        // FIX: Properly register the collection first to satisfy the domain's business rules (horaRecolha)
        entregaService.registarRecolha(pedidoId);

        // Act
        Entrega concluida = entregaService.registarEntregaFinal(pedidoId);

        // Assert
        assertThat(concluida.getEstado()).isEqualTo(EstadoEntrega.ENTREGUE);
        
        // Ensure the driver is free to take new orders
        Entregador driverLibertado = entregadorRepository.findById(entregador.getId()).orElseThrow();
        assertThat(driverLibertado.isAvailable()).isTrue();

        // Verify Kafka event "ENTREGUE" was broadcasted back to the monolith
        verify(entregaAtualizadaPublisher, times(1)).publicar(any(Entrega.class), eq("ENTREGUE"));
    }
    
    @Test
    void iniciarPreparacao_PublishesEventDirectly() {
        // Act
        entregaService.iniciarPreparacao(800L);

        // Assert
        verify(entregaAtualizadaPublisher, times(1)).publicarPedido(800L, "A_PREPARAR");
    }
}