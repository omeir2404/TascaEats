package pt.ul.fc.css.delivery.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import pt.ul.fc.css.delivery.domain.entrega.Entrega;
import pt.ul.fc.css.delivery.domain.entrega.EstadoEntrega;
import pt.ul.fc.css.delivery.domain.entregador.Entregador;
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
class EntregaServiceSelectionTests {

    @Autowired
    private EntregaService entregaService;

    @Autowired
    private EntregaRepository entregaRepository;

    @Autowired
    private EntregadorRepository entregadorRepository;

    @Test
    void selecionarEntregadorPrefereMaiorTempoSemEntrega() {
        Entregador entregadorAntigo = entregadorRepository.save(new Entregador("Entregador 1", "e1@tasca.local", true));
        Entregador entregadorRecente = entregadorRepository.save(new Entregador("Entregador 2", "e2@tasca.local", true));

        LocalDateTime agora = LocalDateTime.now();
        Entrega entregaAntiga = new Entrega(1001L, entregadorAntigo.getId(), "Rua A");
        entregaAntiga.setEstado(EstadoEntrega.ENTREGUE);
        entregaAntiga.setHoraRecolha(agora.minusHours(3));
        entregaAntiga.setHoraEntrega(agora.minusHours(2));
        entregaRepository.save(entregaAntiga);

        Entrega entregaRecente = new Entrega(1002L, entregadorRecente.getId(), "Rua B");
        entregaRecente.setEstado(EstadoEntrega.ENTREGUE);
        entregaRecente.setHoraRecolha(agora.minusHours(2));
        entregaRecente.setHoraEntrega(agora.minusHours(1));
        entregaRepository.save(entregaRecente);

        Entregador selecionado = ReflectionTestUtils.invokeMethod(entregaService, "selecionarEntregadorDisponivel");

        assertThat(selecionado.getId()).isEqualTo(entregadorAntigo.getId());
    }

    @Test
    void selecionarEntregadorSemHistoricoTemPrioridade() {
        Entregador entregadorComHistorico = entregadorRepository.save(new Entregador("Entregador 1", "e3@tasca.local", true));
        Entregador entregadorSemHistorico = entregadorRepository.save(new Entregador("Entregador 2", "e4@tasca.local", true));

        LocalDateTime agora = LocalDateTime.now();
        Entrega entregaAntiga = new Entrega(2001L, entregadorComHistorico.getId(), "Rua C");
        entregaAntiga.setEstado(EstadoEntrega.ENTREGUE);
        entregaAntiga.setHoraRecolha(agora.minusHours(4));
        entregaAntiga.setHoraEntrega(agora.minusHours(3));
        entregaRepository.save(entregaAntiga);

        Entregador selecionado = ReflectionTestUtils.invokeMethod(entregaService, "selecionarEntregadorDisponivel");

        assertThat(selecionado.getId()).isEqualTo(entregadorSemHistorico.getId());
    }
}
