package pt.ul.fc.css.tascaeats.domain;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import pt.ul.fc.css.tascaeats.domain.pagamento.PagamentoCartao;

public class PagamentoCartaoTest {

    @Test
    @DisplayName("PagamentoCartao: validarDetalhes com dados válidos deve passar")
    void validarDetalhesValidosDevePassar() {
        PagamentoCartao pagamento = new PagamentoCartao(1L, 30.0, LocalDate.now(), "REF-1", "1234567890123456");

        assertDoesNotThrow(pagamento::validarDetalhes);
    }

    @Test
    @DisplayName("PagamentoCartao: validarDetalhes com dados inválidos deve falhar")
    void validarDetalhesInvalidosDeveFalhar() {
        PagamentoCartao pagamento = new PagamentoCartao(1L, 30.0, LocalDate.now(), "REF-1", "1234");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, pagamento::validarDetalhes);
        assertEquals("Número de cartão inválido. Deve conter 16 dígitos.", exception.getMessage());
    }

    @Test
    @DisplayName("PagamentoCartao: obterInstrucoesPagamento deve retornar instruções consistentes")
    void obterInstrucoesDeveSerConsistente() {
        PagamentoCartao pagamento = new PagamentoCartao(1L, 30.0, LocalDate.now(), "REF-1", "1234567890123456");
        String instrucoes = pagamento.obterInstrucoesPagamento();

        assertTrue(instrucoes.contains("30.0"));
        assertTrue(instrucoes.contains("3456"));
    }
}
