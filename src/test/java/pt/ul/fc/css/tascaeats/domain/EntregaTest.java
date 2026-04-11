package pt.ul.fc.css.tascaeats.domain;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import pt.ul.fc.css.tascaeats.domain.entrega.Entrega;
import pt.ul.fc.css.tascaeats.domain.pedido.Pedido;

public class EntregaTest {

    @Test
    @DisplayName("Entrega: registarRecolha primeira vez deve definir horaRecolha")
    void registarRecolhaPrimeiraVez() {
        Entrega entrega = entregaNova();

        entrega.registarRecolha();

        assertNotNull(entrega.getHoraRecolha());
    }

    @Test
    @DisplayName("Entrega: registarRecolha segunda vez deve falhar")
    void registarRecolhaSegundaVez() {
        Entrega entrega = entregaNova();
        entrega.registarRecolha();

        IllegalStateException exception = assertThrows(IllegalStateException.class, entrega::registarRecolha);
        assertEquals("A recolha deste pedido já foi registada.", exception.getMessage());
    }

    @Test
    @DisplayName("Entrega: registarConclusao sem recolha deve falhar")
    void registarConclusaoSemRecolha() {
        Entrega entrega = entregaNova();

        IllegalStateException exception = assertThrows(IllegalStateException.class, entrega::registarConclusao);
        assertEquals("Não é possível registar a entrega de um pedido que ainda não foi recolhido.", exception.getMessage());
    }

    @Test
    @DisplayName("Entrega: registarConclusao após recolha deve definir horaEntrega")
    void registarConclusaoAposRecolha() {
        Entrega entrega = entregaNova();
        entrega.registarRecolha();

        entrega.registarConclusao();

        assertNotNull(entrega.getHoraEntrega());
    }

    @Test
    @DisplayName("Entrega: registarConclusao repetida deve falhar")
    void registarConclusaoRepetida() {
        Entrega entrega = entregaNova();
        entrega.registarRecolha();
        entrega.registarConclusao();

        IllegalStateException exception = assertThrows(IllegalStateException.class, entrega::registarConclusao);
        assertEquals("A entrega deste pedido já foi concluída anteriormente.", exception.getMessage());
    }

    @Test
    @DisplayName("Entrega: calcularTempoDeViagem deve devolver duração correta")
    void calcularTempoDeViagem() {
        Entrega entrega = entregaNova();
        entrega.setHoraRecolha(LocalDateTime.of(2026, 4, 11, 10, 0));
        entrega.setHoraEntrega(LocalDateTime.of(2026, 4, 11, 10, 35));

        Duration duracao = entrega.calcularTempoDeViagem();

        assertEquals(Duration.ofMinutes(35), duracao);
    }

    private Entrega entregaNova() {
        Pedido pedido = new Pedido(1L, 1L, 2L);
        return new Entrega(pedido, 99L, "Rua Teste 1");
    }
}
