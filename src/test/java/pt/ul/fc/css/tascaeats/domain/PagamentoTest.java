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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import pt.ul.fc.css.tascaeats.domain.pagamento.Pagamento;
import pt.ul.fc.css.tascaeats.domain.pagamento.PagamentoDinheiro;

public class PagamentoTest {

    @Test
    @DisplayName("Pagamento abstrato: subclasses implementam validarDetalhes e obterInstrucoesPagamento")
    void subclassesDevemImplementarMetodosAbstratos() {
        Pagamento pagamento = new PagamentoDinheiro(1L, 10.0, LocalDate.now(), "REF-1", 10.0);

        pagamento.validarDetalhes();
        String instrucoes = pagamento.obterInstrucoesPagamento();

        assertNotNull(instrucoes);
    }

    @Test
    @DisplayName("Pagamento abstrato: getters e setters devem preservar dados")
    void gettersESettersDevemPreservarDados() {
        Pagamento pagamento = new PagamentoDinheiro(1L, 10.0, LocalDate.of(2026, 4, 11), "REF-1", 10.0);

        pagamento.setId(20L);
        pagamento.setValor(45.5);
        pagamento.setDataPagamento(LocalDate.of(2026, 4, 12));
        pagamento.setReferenciaPagamento("REF-XYZ");

        assertEquals(20L, pagamento.getId());
        assertEquals(45.5, pagamento.getValor());
        assertEquals(LocalDate.of(2026, 4, 12), pagamento.getDataPagamento());
        assertEquals("REF-XYZ", pagamento.getReferenciaPagamento());
    }
}
