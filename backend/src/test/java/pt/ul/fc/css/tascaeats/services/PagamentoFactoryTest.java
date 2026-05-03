// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.services;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import pt.ul.fc.css.tascaeats.domain.pagamento.Pagamento;
import pt.ul.fc.css.tascaeats.domain.pagamento.PagamentoCartao;
import pt.ul.fc.css.tascaeats.domain.pagamento.PagamentoDinheiro;
import pt.ul.fc.css.tascaeats.domain.pagamento.PagamentoMBWay;
import pt.ul.fc.css.tascaeats.domain.pagamento.PagamentoMultibanco;
import pt.ul.fc.css.tascaeats.service.pagamentoServices.PagamentoFactory;

public class PagamentoFactoryTest {

    private final PagamentoFactory pagamentoFactory = new PagamentoFactory();

    // A) criar MBWAY com detalhes válidos deve retornar PagamentoMBWay
    @Test
    @DisplayName("A: criar MBWAY com detalhes válidos deve retornar PagamentoMBWay")
    void criarMBWayValido() {
        Pagamento pagamento = pagamentoFactory.create(
                "MBWAY",
                20.0,
                Map.of("numeroTelemovel", "912345678")
        );

        assertInstanceOf(PagamentoMBWay.class, pagamento);
    }

    // B) criar DINHEIRO com detalhes válidos deve retornar PagamentoDinheiro
    @Test
    @DisplayName("B: criar DINHEIRO com detalhes válidos deve retornar PagamentoDinheiro")
    void criarDinheiroValido() {
        Pagamento pagamento = pagamentoFactory.create(
                "DINHEIRO",
                15.0,
                Map.of("valorRecebido", 20.0)
        );

        assertInstanceOf(PagamentoDinheiro.class, pagamento);
    }

    @Test
    @DisplayName("B2: criar DINHEIRO aceita valor recebido com virgula e euro")
    void criarDinheiroComValorFormatado() {
        Pagamento pagamento = pagamentoFactory.create(
                "DINHEIRO",
                12.5,
                Map.of("valorRecebido", "12,50€")
        );

        assertInstanceOf(PagamentoDinheiro.class, pagamento);
    }

    // C) criar MULTIBANCO com detalhes válidos deve retornar PagamentoMultibanco
    @Test
    @DisplayName("C: criar MULTIBANCO com detalhes válidos deve retornar PagamentoMultibanco")
    void criarMultibancoValido() {
        Pagamento pagamento = pagamentoFactory.create(
                "MULTIBANCO",
                30.0,
                Map.of("entidade", "12345", "referencia", "123456789")
        );

        assertInstanceOf(PagamentoMultibanco.class, pagamento);
    }

    // D) criar CARTAO com detalhes válidos deve retornar PagamentoCartao
    @Test
    @DisplayName("D: criar CARTAO com detalhes válidos deve retornar PagamentoCartao")
    void criarCartaoValido() {
        Pagamento pagamento = pagamentoFactory.create(
                "CARTAO",
                50.0,
                Map.of("numeroCartao", "1234567890123456")
        );

        assertInstanceOf(PagamentoCartao.class, pagamento);
    }

    // E) tipo de pagamento inválido deve lançar exceção
    @Test
    @DisplayName("E: tipo de pagamento inválido deve lançar exceção")
    void tipoPagamentoInvalido() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> pagamentoFactory.create("CRYPTO", 10.0, Map.of("wallet", "abc"))
        );

        assertEquals("Tipo de pagamento não suportado: CRYPTO", exception.getMessage());
    }

    // F) detalhes obrigatórios ausentes por tipo devem lançar exceção
    @Test
    @DisplayName("F: detalhes obrigatórios ausentes por tipo devem lançar exceção")
    void detalhesObrigatoriosAusentesPorTipo() {
        IllegalArgumentException mbwayException = assertThrows(
                IllegalArgumentException.class,
                () -> pagamentoFactory.create("MBWAY", 10.0, Map.of())
        );
        assertEquals("Campo obrigatório em detalhes: numeroTelemovel", mbwayException.getMessage());

        IllegalArgumentException dinheiroException = assertThrows(
                IllegalArgumentException.class,
                () -> pagamentoFactory.create("DINHEIRO", 10.0, Map.of())
        );
        assertEquals("Campo obrigatório em detalhes: valorRecebido", dinheiroException.getMessage());

        IllegalArgumentException multibancoException = assertThrows(
                IllegalArgumentException.class,
                () -> pagamentoFactory.create("MULTIBANCO", 10.0, Map.of())
        );
        assertEquals("Campo obrigatório em detalhes: entidade", multibancoException.getMessage());

        IllegalArgumentException cartaoException = assertThrows(
                IllegalArgumentException.class,
                () -> pagamentoFactory.create("CARTAO", 10.0, Map.of())
        );
        assertEquals("Campo obrigatório em detalhes: numeroCartao", cartaoException.getMessage());
    }
}
