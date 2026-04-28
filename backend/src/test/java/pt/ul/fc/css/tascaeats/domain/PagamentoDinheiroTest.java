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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import pt.ul.fc.css.tascaeats.domain.pagamento.PagamentoDinheiro;

public class PagamentoDinheiroTest {

    @Test
    @DisplayName("PagamentoDinheiro: validarDetalhes com valor recebido suficiente deve calcular troco")
    void validarDetalhesSuficienteDeveCalcularTroco() {
        PagamentoDinheiro pagamento = new PagamentoDinheiro(1L, 12.5, LocalDate.now(), "REF-1", 20.0);

        pagamento.validarDetalhes();

        assertEquals(7.5, pagamento.getTroco());
    }

    @Test
    @DisplayName("PagamentoDinheiro: validarDetalhes com valor insuficiente deve falhar")
    void validarDetalhesInsuficienteDeveFalhar() {
        PagamentoDinheiro pagamento = new PagamentoDinheiro(1L, 12.5, LocalDate.now(), "REF-1", 10.0);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, pagamento::validarDetalhes);
        assertEquals("Valor recebido insuficiente para concluir o pagamento em dinheiro.", exception.getMessage());
    }

    @Test
    @DisplayName("PagamentoDinheiro: obterInstrucoesPagamento deve incluir troco esperado")
    void obterInstrucoesDeveIncluirTroco() {
        PagamentoDinheiro pagamento = new PagamentoDinheiro(1L, 10.0, LocalDate.now(), "REF-1", 15.0);
        pagamento.validarDetalhes();

        String instrucoes = pagamento.obterInstrucoesPagamento();
        assertTrue(instrucoes.contains("Troco previsto"));
        assertTrue(instrucoes.contains("5.0"));
    }
}
