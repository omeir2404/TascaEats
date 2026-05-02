// ##############################################################
//                        TASCAEATS                             #
//                      CSS GRUPO: 25                           #
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.services;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import pt.ul.fc.css.tascaeats.domain.entrega.Entrega;
import pt.ul.fc.css.tascaeats.domain.pedido.EstadoPedido;
import pt.ul.fc.css.tascaeats.domain.pedido.Pedido;
import pt.ul.fc.css.tascaeats.domain.utilizador.Entregador;
import pt.ul.fc.css.tascaeats.repository.entregaRepository.IEntregaRepository;
import pt.ul.fc.css.tascaeats.repository.pedidoRepository.IPedidoRepository;
import pt.ul.fc.css.tascaeats.repository.utilizadorRepository.IUtilizadorRepository;
import pt.ul.fc.css.tascaeats.service.entregaService.EntregaService;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
public class EntregaServiceTest {

    @Mock
    private IEntregaRepository entregaRepository;

    @Mock
    private IUtilizadorRepository utilizadorRepository;

    @Mock
    private IPedidoRepository pedidoRepository;

    @InjectMocks
    private EntregaService entregaService;


    @Test
    @DisplayName("Auto-1: Marcar pedido como pronto deve gatilhar atribuição automática.")
    void testMarcarPedidoComoProntoTriggersAutomaticAssignment() {
        Pedido order = pedidoComEstado(1L, EstadoPedido.A_PREPARAR);
        Entregador deliverer = entregador(200L, true);

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(order));
        when(utilizadorRepository.findAll()).thenReturn(Arrays.asList(deliverer));
        when(entregaRepository.existsByEntregadorIdAndHoraEntregaIsNull(200L)).thenReturn(false);
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(i -> i.getArgument(0));
        when(entregaRepository.save(any(Entrega.class))).thenAnswer(i -> i.getArgument(0));

        Pedido updatedOrder = entregaService.marcarPedidoComoPronto(1L);

        assertEquals(EstadoPedido.ATRIBUIDO_ENTREGADOR, updatedOrder.getEstado());
        assertFalse(deliverer.isAvailable());
        verify(pedidoRepository, atLeastOnce()).save(order);
        verify(utilizadorRepository).save(deliverer);
        verify(entregaRepository).save(any(Entrega.class));
    }

    @Test
    @DisplayName("Auto-2: Marcar pedido como pronto deve falhar se não houver entregador disponível.")
    void testMarcarPedidoComoProntoThrowsExceptionWhenNoDelivererAvailable() {
        Pedido order = pedidoComEstado(1L, EstadoPedido.A_PREPARAR);
        Entregador deliverer = entregador(200L, false);

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(order));
        when(utilizadorRepository.findAll()).thenReturn(Arrays.asList(deliverer));

        assertThrows(IllegalStateException.class, () -> {
            entregaService.marcarPedidoComoPronto(1L);
        });
    }

    // ########################### PREPARAÇÃO E RECOLHA ###########################

    @Test
    @DisplayName("K: Iniciar preparação com estado PAGO deve passar e gravar A_PREPARAR.")
    void iniciarPreparacaoComPago() {
        Pedido pedido = pedidoComEstado(57L, EstadoPedido.PAGO);
        when(pedidoRepository.findById(57L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(i -> i.getArgument(0));

        Pedido atualizado = entregaService.iniciarPreparacao(57L);
        assertEquals(EstadoPedido.A_PREPARAR, atualizado.getEstado());
        verify(pedidoRepository).save(pedido);
    }

    @Test
    @DisplayName("P: Registar recolha com sucesso deve definir horaRecolha.")
    void registarRecolhaSuccessDefineHoraRecolha() {
        Pedido pedido = pedidoComEstado(72L, EstadoPedido.ATRIBUIDO_ENTREGADOR);
        Entrega entrega = entregaParaPedido(pedido, 901L);
        when(entregaRepository.findByPedidoId(72L)).thenReturn(Optional.of(entrega));
        when(pedidoRepository.findById(72L)).thenReturn(Optional.of(pedido));
        when(entregaRepository.save(any(Entrega.class))).thenAnswer(i -> i.getArgument(0));

        Entrega atualizada = entregaService.registarRecolha(72L);
        assertNotNull(atualizada.getHoraRecolha());
        verify(entregaRepository).save(entrega);
    }

    @Test
    @DisplayName("S/T/U: Registar entrega final com sucesso define horaEntrega, muda pedido para ENTREGUE e liberta entregador.")
    void registarEntregaFinalSuccess() {
        Pedido pedido = pedidoComEstado(82L, EstadoPedido.ATRIBUIDO_ENTREGADOR);
        Entrega entrega = entregaParaPedido(pedido, 991L);
        entrega.setHoraRecolha(java.time.LocalDateTime.now());
        Entregador entregador = entregador(991L, false);

        when(entregaRepository.findByPedidoId(82L)).thenReturn(Optional.of(entrega));
        when(pedidoRepository.findById(82L)).thenReturn(Optional.of(pedido));
        when(utilizadorRepository.findById(991L)).thenReturn(Optional.of(entregador));
        when(utilizadorRepository.save(any(Entregador.class))).thenAnswer(i -> i.getArgument(0));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(i -> i.getArgument(0));
        when(entregaRepository.save(any(Entrega.class))).thenAnswer(i -> i.getArgument(0));

        Entrega atualizada = entregaService.registarEntregaFinal(82L);
        assertNotNull(atualizada.getHoraEntrega());
        assertEquals(EstadoPedido.ENTREGUE, pedido.getEstado());
        assertTrue(entregador.isAvailable());
        verify(utilizadorRepository).save(entregador);
        verify(pedidoRepository).save(pedido);
        verify(entregaRepository).save(entrega);
    }

    // ########################### HELPER METHODS ###########################

    private Pedido pedidoComEstado(Long id, EstadoPedido estado) {
        Pedido pedido = new Pedido(id, 100L); 
        pedido.setEstado(estado);
        return pedido;
    }

    private Entregador entregador(Long id, boolean available) {
        Entregador entregador = new Entregador();
        entregador.setId(id);
        entregador.setAvailable(available);
        return entregador;
    }

    private Entrega entregaParaPedido(Pedido pedido, Long entregadorId) {
        return new Entrega(pedido, entregadorId, "Rua Teste 1");
    }
}