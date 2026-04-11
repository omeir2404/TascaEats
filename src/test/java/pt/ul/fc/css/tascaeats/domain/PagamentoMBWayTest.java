// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.domain;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import pt.ul.fc.css.tascaeats.domain.pagamento.PagamentoMBWay;

public class PagamentoMBWayTest {

    @Test
    @DisplayName("PagamentoMBWay: validarDetalhes com número válido deve passar")
    void validarDetalhesNumeroValidoDevePassar() {
        PagamentoMBWay pagamento = new PagamentoMBWay(1L, 20.0, LocalDate.now(), "REF-1", "912345678");

        assertDoesNotThrow(pagamento::validarDetalhes);
    }

    @Test
    @DisplayName("PagamentoMBWay: validarDetalhes com número inválido deve falhar")
    void validarDetalhesNumeroInvalidoDeveFalhar() {
        PagamentoMBWay pagamento = new PagamentoMBWay(1L, 20.0, LocalDate.now(), "REF-1", "812345678");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, pagamento::validarDetalhes);
        assertEquals("Número de telemóvel inválido para MBWay.", exception.getMessage());
    }

    @Test
    @DisplayName("PagamentoMBWay: obterInstrucoesPagamento deve incluir valor e número")
    void obterInstrucoesDeveIncluirValorENumero() {
        PagamentoMBWay pagamento = new PagamentoMBWay(1L, 20.0, LocalDate.now(), "REF-1", "912345678");
        String instrucoes = pagamento.obterInstrucoesPagamento();

        assertTrue(instrucoes.contains("20.0"));
        assertTrue(instrucoes.contains("912345678"));
    }
}
