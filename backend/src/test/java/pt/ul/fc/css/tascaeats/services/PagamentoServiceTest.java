// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.ul.fc.css.tascaeats.domain.pagamento.Pagamento;
import pt.ul.fc.css.tascaeats.domain.pagamento.PagamentoDinheiro;
import pt.ul.fc.css.tascaeats.domain.pedido.EstadoPedido;
import pt.ul.fc.css.tascaeats.domain.pedido.Pedido;
import pt.ul.fc.css.tascaeats.dto.pagamentoDTO.ProcessarPagamentoDTO;
import pt.ul.fc.css.tascaeats.publishers.PedidoPagoPublisher;
import pt.ul.fc.css.tascaeats.repository.pagamentoRepository.IPagamentoRepository;
import pt.ul.fc.css.tascaeats.repository.pedidoRepository.IPedidoRepository;
import pt.ul.fc.css.tascaeats.repository.utilizadorRepository.IClienteRepository;
import pt.ul.fc.css.tascaeats.service.pagamentoServices.PagamentoFactory;
import pt.ul.fc.css.tascaeats.service.pagamentoServices.PagamentoService;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Fail.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
public class PagamentoServiceTest {

    @Mock
    private IPedidoRepository pedidoRepository;

    @Mock
    private IPagamentoRepository pagamentoRepository;

    @Mock
    private PagamentoFactory pagamentoFactory;

    @Mock
    private IClienteRepository clienteRepository;

    @Mock
    private PedidoPagoPublisher pedidoPagoPublisher;

    @InjectMocks
    private PagamentoService pagamentoService;

    // A) processarPagamento com pedidoId nulo deve falhar
    @Test
    @DisplayName("A: Processar pagamento com pedidoId nulo deve falhar.")
    void processarPagamentoWithPedidoIdNulo() {
        ProcessarPagamentoDTO dto = new ProcessarPagamentoDTO(null, "DINHEIRO", Map.of("valorRecebido", 20.0));

        try {
            pagamentoService.processarPagamento(dto);
            fail("Pedido ID é obrigatório.");
        } catch (IllegalArgumentException e) {
            assertEquals("Pedido ID é obrigatório", e.getMessage());
        }
    }

    // B) processarPagamento com pedido inexistente deve falhar
    @Test
    @DisplayName("B: Processar pagamento com pedido inexistente deve falhar.")
    void processarPagamentoWithPedidoInexistente() {
        ProcessarPagamentoDTO dto = new ProcessarPagamentoDTO(99L, "DINHEIRO", Map.of("valorRecebido", 20.0));
        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

        try {
            pagamentoService.processarPagamento(dto);
            fail("Pedido não encontrado.");
        } catch (IllegalArgumentException e) {
            assertEquals("Pedido não encontrado", e.getMessage());
        }
    }

    // C) processarPagamento com factory a devolver null deve falhar
    @Test
    @DisplayName("C: Processar pagamento com factory a devolver null deve falhar.")
    void processarPagamentoWithFactoryNull() {
        Pedido pedido = pedidoCriado(10L, 30.0);
        ProcessarPagamentoDTO dto = new ProcessarPagamentoDTO(10L, "DINHEIRO", Map.of("valorRecebido", 30.0));

        when(pedidoRepository.findById(10L)).thenReturn(Optional.of(pedido));
        when(pagamentoFactory.create("DINHEIRO", 30.0, Map.of("valorRecebido", 30.0))).thenReturn(null);

        try {
            pagamentoService.processarPagamento(dto);
            fail("Falha ao criar pagamento.");
        } catch (IllegalArgumentException e) {
            assertEquals("Falha ao criar pagamento", e.getMessage());
        }

        verify(pagamentoRepository, never()).save(any(Pagamento.class));
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    // D/E/F) processarPagamento com sucesso deve guardar pagamento, associar ao pedido e mudar estado para PAGO
    @Test
    @DisplayName("D/E/F: Processar pagamento com sucesso deve guardar pagamento, associar ao pedido e mudar estado para PAGO.")
    void processarPagamentoSuccess() {
        Pedido pedido = pedidoCriado(11L, 45.0);
        ProcessarPagamentoDTO dto = new ProcessarPagamentoDTO(11L, "DINHEIRO", Map.of("valorRecebido", 50.0));
        Pagamento pagamento = new PagamentoDinheiro(null, 45.0, LocalDate.now(), "REF-001", 50.0);

        when(pedidoRepository.findById(11L)).thenReturn(Optional.of(pedido));
        when(pagamentoFactory.create("DINHEIRO", 45.0, Map.of("valorRecebido", 50.0))).thenReturn(pagamento);
        when(pagamentoRepository.save(any(Pagamento.class))).thenAnswer(i -> i.getArgument(0));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(i -> i.getArgument(0));
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        pagamentoService.processarPagamento(dto);

        verify(pagamentoRepository).save(pagamento);
        verify(pedidoRepository).save(pedido);
        assertNotNull(pedido.getPagamento());
        assertEquals(EstadoPedido.PAGO, pedido.getEstado());
    }

    // G) processarPagamento não deve permitir pagar pedido já pago (via regra de domínio)
    @Test
    @DisplayName("G: Processar pagamento não deve permitir pagar pedido já pago.")
    void processarPagamentoPedidoJaPago() {
        Pedido pedido = pedidoCriado(12L, 60.0);
        pedido.setEstado(EstadoPedido.PAGO);

        ProcessarPagamentoDTO dto = new ProcessarPagamentoDTO(12L, "DINHEIRO", Map.of("valorRecebido", 60.0));

        when(pedidoRepository.findById(12L)).thenReturn(Optional.of(pedido));

        try {
            pagamentoService.processarPagamento(dto);
            fail("Pedido já pago não pode ser pago novamente.");
        } catch (IllegalStateException e) {
            assertEquals("O pedido só pode ser pago se estiver no estado CRIADO.", e.getMessage());
        }

        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    private Pedido pedidoCriado(Long id, double valorTotal) {
        Pedido pedido = new Pedido(id, 1L, 2L);
        pedido.setEstado(EstadoPedido.CRIADO);
        pedido.setValorTotal(valorTotal);
        return pedido;
    }
}
