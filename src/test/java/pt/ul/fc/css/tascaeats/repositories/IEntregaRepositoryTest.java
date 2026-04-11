package pt.ul.fc.css.tascaeats.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import jakarta.persistence.EntityManager;
import pt.ul.fc.css.tascaeats.domain.entrega.Entrega;
import pt.ul.fc.css.tascaeats.domain.pedido.Pedido;
import pt.ul.fc.css.tascaeats.repository.entregaRepository.IEntregaRepository;

@DataJpaTest
public class IEntregaRepositoryTest {

    @Autowired
    private IEntregaRepository entregaRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("A: findByEntregadorId deve devolver entregas do entregador")
    void findByEntregadorIdDeveDevolverEntregasDoEntregador() {
        guardarEntrega(900L, false);
        guardarEntrega(900L, true);
        guardarEntrega(901L, false);

        List<Entrega> resultados = entregaRepository.findByEntregadorId(900L);

        assertThat(resultados).hasSize(2);
        assertThat(resultados).allMatch(e -> e.getEntregadorId().equals(900L));
    }

    @Test
    @DisplayName("B: findByPedidoId deve devolver entrega associada ao pedido")
    void findByPedidoIdDeveDevolverEntregaAssociadaAoPedido() {
        Entrega entrega = guardarEntrega(910L, false);

        Optional<Entrega> resultado = entregaRepository.findByPedidoId(entrega.getPedido().getId());

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(entrega.getId());
    }

    @Test
    @DisplayName("C: findByHoraEntregaIsNull deve devolver entregas ativas")
    void findByHoraEntregaIsNullDeveDevolverEntregasAtivas() {
        Entrega ativa1 = guardarEntrega(920L, false);
        Entrega ativa2 = guardarEntrega(921L, false);
        guardarEntrega(922L, true);

        List<Entrega> resultados = entregaRepository.findByHoraEntregaIsNull();

        assertThat(resultados).extracting(Entrega::getId)
                .contains(ativa1.getId(), ativa2.getId());
        assertThat(resultados).allMatch(e -> e.getHoraEntrega() == null);
    }

    @Test
    @DisplayName("D: existsByEntregadorIdAndHoraEntregaIsNull deve refletir estado ativo do entregador")
    void existsByEntregadorIdAndHoraEntregaIsNullDeveRefletirEstadoAtivo() {
        guardarEntrega(930L, false);
        guardarEntrega(931L, true);

        boolean entregador930Ativo = entregaRepository.existsByEntregadorIdAndHoraEntregaIsNull(930L);
        boolean entregador931Ativo = entregaRepository.existsByEntregadorIdAndHoraEntregaIsNull(931L);

        assertThat(entregador930Ativo).isTrue();
        assertThat(entregador931Ativo).isFalse();
    }

    private Entrega guardarEntrega(Long entregadorId, boolean finalizada) {
        Pedido pedido = new Pedido(null, 1L, 2L);
        entityManager.persist(pedido);

        Entrega entrega = new Entrega(pedido, entregadorId, "Rua Teste 1");
        entrega.setHoraRecolha(LocalDateTime.now().minusMinutes(30));

        if (finalizada) {
            entrega.setHoraEntrega(LocalDateTime.now());
        }

        entityManager.persist(entrega);
        entityManager.flush();
        return entrega;
    }
}
