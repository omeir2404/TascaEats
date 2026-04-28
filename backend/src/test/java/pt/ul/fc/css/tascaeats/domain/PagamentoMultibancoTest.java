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

import pt.ul.fc.css.tascaeats.domain.pagamento.PagamentoMultibanco;

public class PagamentoMultibancoTest {

    @Test
    @DisplayName("PagamentoMultibanco: validarDetalhes com entidade/referência válidas deve passar")
    void validarDetalhesValidosDevePassar() {
        PagamentoMultibanco pagamento = new PagamentoMultibanco(1L, 20.0, LocalDate.now(), "REF-1", "12345", "123456789");

        assertDoesNotThrow(pagamento::validarDetalhes);
    }

    @Test
    @DisplayName("PagamentoMultibanco: validarDetalhes com entidade/referência inválidas deve falhar")
    void validarDetalhesInvalidosDeveFalhar() {
        PagamentoMultibanco pagamento = new PagamentoMultibanco(1L, 20.0, LocalDate.now(), "REF-1", "12", "ABC");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, pagamento::validarDetalhes);
        assertEquals("Entidade inválida. Deve conter 5 dígitos.", exception.getMessage());
    }

    @Test
    @DisplayName("PagamentoMultibanco: obterInstrucoesPagamento deve conter entidade e referência")
    void obterInstrucoesDeveConterEntidadeEReferencia() {
        PagamentoMultibanco pagamento = new PagamentoMultibanco(1L, 20.0, LocalDate.now(), "REF-1", "12345", "123456789");
        String instrucoes = pagamento.obterInstrucoesPagamento();

        assertTrue(instrucoes.contains("12345"));
        assertTrue(instrucoes.contains("123456789"));
    }
}
