package pt.ul.fc.css.tascaeats.repositories;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import jakarta.persistence.EntityManager;
import pt.ul.fc.css.tascaeats.domain.pagamento.Pagamento;
import pt.ul.fc.css.tascaeats.domain.pagamento.PagamentoCartao;
import pt.ul.fc.css.tascaeats.domain.pagamento.PagamentoDinheiro;
import pt.ul.fc.css.tascaeats.domain.pagamento.PagamentoMBWay;
import pt.ul.fc.css.tascaeats.domain.pagamento.PagamentoMultibanco;
import pt.ul.fc.css.tascaeats.repository.pagamentoRepository.IPagamentoRepository;

@DataJpaTest
@SuppressWarnings("null")
public class IPagamentoRepositoryTest {

    @Autowired
    private IPagamentoRepository pagamentoRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("A: guardar e obter pagamento por id")
    void guardarEObterPagamentoPorId() {
        PagamentoMBWay pagamento = new PagamentoMBWay(null, 25.0, LocalDate.now(), "REF-MB1", "912345678");
        pagamento.validarDetalhes();

        entityManager.persist(pagamento);
        entityManager.flush();

        Pagamento encontrado = pagamentoRepository.findById(pagamento.getId()).orElse(null);

        assertThat(encontrado).isNotNull();
        assertThat(encontrado.getId()).isEqualTo(pagamento.getId());
        assertThat(encontrado.getReferenciaPagamento()).isEqualTo("REF-MB1");
    }

    @Test
    @DisplayName("B/C: persistência por subtipo e discriminação correta no retrieval")
    void persistenciaPorSubtipoERecuperacaoComTipoCorreto() {
        PagamentoMBWay mbway = new PagamentoMBWay(null, 10.0, LocalDate.now(), "REF-MB2", "923456789");
        PagamentoDinheiro dinheiro = new PagamentoDinheiro(null, 12.0, LocalDate.now(), "REF-DIN1", 20.0);
        PagamentoMultibanco multibanco = new PagamentoMultibanco(null, 14.0, LocalDate.now(), "REF-MULT1", "12345", "123456789");
        PagamentoCartao cartao = new PagamentoCartao(null, 16.0, LocalDate.now(), "REF-CAR1", "1234567890123456");

        mbway.validarDetalhes();
        dinheiro.validarDetalhes();
        multibanco.validarDetalhes();
        cartao.validarDetalhes();

        entityManager.persist(mbway);
        entityManager.persist(dinheiro);
        entityManager.persist(multibanco);
        entityManager.persist(cartao);
        entityManager.flush();

        Pagamento p1 = pagamentoRepository.findById(mbway.getId()).orElseThrow();
        Pagamento p2 = pagamentoRepository.findById(dinheiro.getId()).orElseThrow();
        Pagamento p3 = pagamentoRepository.findById(multibanco.getId()).orElseThrow();
        Pagamento p4 = pagamentoRepository.findById(cartao.getId()).orElseThrow();

        assertThat(p1).isInstanceOf(PagamentoMBWay.class);
        assertThat(p2).isInstanceOf(PagamentoDinheiro.class);
        assertThat(p3).isInstanceOf(PagamentoMultibanco.class);
        assertThat(p4).isInstanceOf(PagamentoCartao.class);
    }
}
