// ##############################################################
// 					  TASCAEATS						#
// 					CSS GRUPO: 25						#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.delivery.service;

import java.time.LocalDateTime;
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
import pt.ul.fc.css.delivery.dto.entregaDTO.AtualizarEstadoEntregaDTO;
import pt.ul.fc.css.delivery.dto.entregaDTO.AtribuirEntregadorDTO;
import pt.ul.fc.css.delivery.dto.entregadorDTO.AtualizarEntregadorDTO;
import pt.ul.fc.css.delivery.dto.entregadorDTO.CriarEntregadorDTO;
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
        entregadorRepository.save(new Entregador("Joao", "joao@tasca.local", true));
        entregadorRepository.save(new Entregador("Maria", "maria@tasca.local", false));

        List<Entregador> resultados = entregaService.listarEntregadores();

        assertThat(resultados).hasSize(2);
    }

    @Test
    void registarEntregador_RejectsNullDto() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> entregaService.registarEntregador(null));

        assertThat(exception.getMessage()).isEqualTo("Entregador nao pode ser nulo.");
    }

    @Test
    void registarEntregador_RejectsBlankEmail() {
        CriarEntregadorDTO dto = new CriarEntregadorDTO("Rita", " ");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> entregaService.registarEntregador(dto));

        assertThat(exception.getMessage()).isEqualTo("Email do entregador e obrigatorio.");
    }

    @Test
    void atualizarEntregador_AtualizaCamposIndicados() {
        Entregador entregador = entregadorRepository.save(new Entregador("Miguel", "miguel@tasca.local", true));
        AtualizarEntregadorDTO dto = new AtualizarEntregadorDTO("Miguel Silva", null, Boolean.FALSE);

        Entregador atualizado = entregaService.atualizarEntregador(entregador.getId(), dto);

        assertThat(atualizado.getNome()).isEqualTo("Miguel Silva");
        assertThat(atualizado.getEmail()).isEqualTo("miguel@tasca.local");
        assertThat(atualizado.isAvailable()).isFalse();
    }

    @Test
    void buscarEntregadores_ComNomeEDisponibilidade_FiltraCorretamente() {
        Entregador primeiro = entregadorRepository.save(new Entregador("Ana", "ana@tasca.local", true));
        entregadorRepository.save(new Entregador("Ana Paula", "ana.paula@tasca.local", false));
        entregadorRepository.save(new Entregador("Carlos", "carlos@tasca.local", true));

        List<Entregador> resultados = entregaService.buscarEntregadores("ana", true);

        assertThat(resultados).extracting(Entregador::getId).containsExactly(primeiro.getId());
    }

    @Test
    void buscarEntregadores_SemFiltros_DevolveTodos() {
        entregadorRepository.save(new Entregador("Teresa", "teresa@tasca.local", true));
        entregadorRepository.save(new Entregador("Bruno", "bruno@tasca.local", false));

        List<Entregador> resultados = entregaService.buscarEntregadores(null, null);

        assertThat(resultados).hasSize(2);
    }

    @Test
    void obterEntregadorPorEmail_RejectsBlankEmail() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> entregaService.obterEntregadorPorEmail(" "));

        assertThat(exception.getMessage()).isEqualTo("Email do entregador invalido.");
    }

    @Test
    void atribuirEntregador_WithAvailableDriver_Success() {
        Entregador entregador = entregadorRepository.save(new Entregador("Carlos", "carlos@tasca.local", true));
        Long pedidoId = 500L;
        AtribuirEntregadorDTO dto = new AtribuirEntregadorDTO(pedidoId);

        Entrega entrega = entregaService.atribuirEntregador(dto);

        assertThat(entrega).isNotNull();
        assertThat(entrega.getPedidoId()).isEqualTo(pedidoId);
        assertThat(entrega.getEntregadorId()).isEqualTo(entregador.getId());

        Entregador updatedDriver = entregadorRepository.findById(entregador.getId()).orElseThrow();
        assertThat(updatedDriver.isAvailable()).isFalse();

        verify(entregaAtualizadaPublisher, times(1)).publicar(any(Entrega.class), eq("ATRIBUIDO_ENTREGADOR"));
    }

    @Test
    void atribuirEntregador_UsaEnderecoPendenteQuandoNaoInformado() {
        Entregador entregador = entregadorRepository.save(new Entregador("Paulo", "paulo@tasca.local", true));

        Entrega entrega = entregaService.atribuirEntregador(new AtribuirEntregadorDTO(900L));

        assertThat(entrega.getEntregadorId()).isEqualTo(entregador.getId());
        assertThat(entrega.getEnderecoEntrega()).isEqualTo("ENDERECO_NAO_INFORMADO");
    }

    @Test
    void atribuirEntregador_WithNoAvailableDrivers_ThrowsException() {
        entregadorRepository.save(new Entregador("Carlos", "carlos@tasca.local", false));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> entregaService.atribuirEntregador(501L));

        assertThat(exception.getMessage()).isEqualTo("Nao ha entregadores disponiveis de momento.");
    }

    @Test
    void registarRecolha_UpdatesStateAndPublishesEvent() {
        Entregador entregador = entregadorRepository.save(new Entregador("Ana", "ana@tasca.local", false));
        Long pedidoId = 600L;
        entregaRepository.save(new Entrega(pedidoId, entregador.getId(), "Rua Principal"));

        Entrega atualizada = entregaService.registarRecolha(pedidoId);

        assertThat(atualizada.getEstado()).isEqualTo(EstadoEntrega.A_CAMINHO);
        verify(entregaAtualizadaPublisher, times(1)).publicar(any(Entrega.class), eq("ATRIBUIDO_ENTREGADOR"));
    }

    @Test
    void marcarPronto_ComEntregaExistente_PublicaEntregaDireta() {
        Entregador entregador = entregadorRepository.save(new Entregador("Ines", "ines@tasca.local", false));
        entregaRepository.save(new Entrega(1000L, entregador.getId(), "Rua Azul"));

        entregaService.marcarPronto(1000L);

        verify(entregaAtualizadaPublisher, times(1)).publicar(any(Entrega.class), eq("PRONTO_PARA_ENTREGA"));
    }

    @Test
    void registarEntregaFinal_FreesDriverAndPublishesEvent() {
        Entregador entregador = entregadorRepository.save(new Entregador("Pedro", "pedro@tasca.local", false));
        Long pedidoId = 700L;
        entregaRepository.save(new Entrega(pedidoId, entregador.getId(), "Rua Destino"));

        entregaService.registarRecolha(pedidoId);

        Entrega concluida = entregaService.registarEntregaFinal(pedidoId);

        assertThat(concluida.getEstado()).isEqualTo(EstadoEntrega.ENTREGUE);

        Entregador driverLibertado = entregadorRepository.findById(entregador.getId()).orElseThrow();
        assertThat(driverLibertado.isAvailable()).isTrue();

        verify(entregaAtualizadaPublisher, times(1)).publicar(any(Entrega.class), eq("ENTREGUE"));
    }

    @Test
    void atualizarEstadoEntrega_ParaEntregueLibertaEntregador() {
        Entregador entregador = entregadorRepository.save(new Entregador("Sofia", "sofia@tasca.local", false));
        entregaRepository.save(new Entrega(1100L, entregador.getId(), "Rua Verde"));
        entregaService.registarRecolha(1100L);

        Entrega atualizada = entregaService.atualizarEstadoEntrega(1100L,
                new AtualizarEstadoEntregaDTO(EstadoEntrega.ENTREGUE));

        assertThat(atualizada.getEstado()).isEqualTo(EstadoEntrega.ENTREGUE);
        assertThat(entregadorRepository.findById(entregador.getId()).orElseThrow().isAvailable()).isTrue();
        verify(entregaAtualizadaPublisher, times(1)).publicar(any(Entrega.class), eq("ENTREGUE"));
    }

    @Test
    void listarEntregasEmCurso_DevolveApenasEntregasSemConclusao() {
        Entregador entregador = entregadorRepository.save(new Entregador("Rui", "rui@tasca.local", true));

        Entrega emCurso = entregaRepository.save(new Entrega(1200L, entregador.getId(), "Rua Um"));
        Entrega concluida = new Entrega(1201L, entregador.getId(), "Rua Dois");
        concluida.setHoraRecolha(LocalDateTime.now().minusMinutes(40));
        concluida.setHoraEntrega(LocalDateTime.now().minusMinutes(10));
        concluida.setEstado(EstadoEntrega.ENTREGUE);
        entregaRepository.save(concluida);

        List<Entrega> resultados = entregaService.listarEntregasEmCurso();

        assertThat(resultados).extracting(Entrega::getPedidoId).containsExactly(emCurso.getPedidoId());
    }

    @Test
    void iniciarPreparacao_PublishesEventDirectly() {
        entregaService.iniciarPreparacao(800L);

        verify(entregaAtualizadaPublisher, times(1)).publicarPedido(800L, "A_PREPARAR");
    }
}
