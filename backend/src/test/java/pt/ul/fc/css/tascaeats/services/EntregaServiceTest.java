// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.services;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Fail.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import pt.ul.fc.css.tascaeats.domain.entrega.Entrega;
import pt.ul.fc.css.tascaeats.domain.pedido.EstadoPedido;
import pt.ul.fc.css.tascaeats.domain.pedido.Pedido;
import pt.ul.fc.css.tascaeats.domain.utilizador.Cliente;
import pt.ul.fc.css.tascaeats.domain.utilizador.Entregador;
import pt.ul.fc.css.tascaeats.dto.entregaDTO.AtribuirEntregadorDTO;
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

    // A) atribuirEntregador com DTO nulo deve falhar
    @Test
    @DisplayName("A: Atribuir entregador com DTO nulo deve falhar.")
    void atribuirEntregadorWithNullDTO() {
        try {
            entregaService.atribuirEntregador(null);
            fail("DTO inválido.");
        } catch (IllegalArgumentException e) {
            assertEquals("Pedido de atribuição inválido.", e.getMessage());
        }
    }

    // B) atribuirEntregador com pedidoId nulo deve falhar
    @Test
    @DisplayName("B: Atribuir entregador com pedidoId nulo deve falhar.")
    void atribuirEntregadorWithNullPedidoId() {
        AtribuirEntregadorDTO dto = new AtribuirEntregadorDTO(null);

        try {
            entregaService.atribuirEntregador(dto);
            fail("ID do pedido inválido.");
        } catch (IllegalArgumentException e) {
            assertEquals("ID do pedido inválido.", e.getMessage());
        }
    }

    // C) atribuirEntregador com pedido inexistente deve falhar
    @Test
    @DisplayName("C: Atribuir entregador com pedido inexistente deve falhar.")
    void atribuirEntregadorWithPedidoInexistente() {
        when(pedidoRepository.findById(999L)).thenReturn(Optional.empty());

        try {
            entregaService.atribuirEntregador(new AtribuirEntregadorDTO(999L));
            fail("Pedido inexistente.");
        } catch (IllegalArgumentException e) {
            assertEquals("Pedido não encontrado: 999", e.getMessage());
        }
    }

    // D) atribuirEntregador com pedido fora de PRONTO_PARA_ENTREGA deve falhar
    @Test
    @DisplayName("D: Atribuir entregador com pedido fora de PRONTO_PARA_ENTREGA deve falhar.")
    void atribuirEntregadorWithPedidoEstadoInvalido() {
        Pedido pedido = pedidoComEstado(10L, EstadoPedido.PAGO);
        when(pedidoRepository.findById(10L)).thenReturn(Optional.of(pedido));

        try {
            entregaService.atribuirEntregador(new AtribuirEntregadorDTO(10L));
            fail("Pedido não está pronto para entrega.");
        } catch (IllegalStateException e) {
            assertEquals("Pedido não está pronto para entrega.", e.getMessage());
        }
    }

    // E) atribuirEntregador com entrega já existente para o pedido deve falhar
    @Test
    @DisplayName("E: Atribuir entregador com entrega já existente para o pedido deve falhar.")
    void atribuirEntregadorWithEntregaJaExistente() {
        Pedido pedido = pedidoComEstado(20L, EstadoPedido.PRONTO_PARA_ENTREGA);
        Entrega entrega = entregaParaPedido(pedido, 500L);

        when(pedidoRepository.findById(20L)).thenReturn(Optional.of(pedido));
        when(entregaRepository.findByPedidoId(20L)).thenReturn(Optional.of(entrega));

        try {
            entregaService.atribuirEntregador(new AtribuirEntregadorDTO(20L));
            fail("Pedido já possui entrega associada.");
        } catch (IllegalStateException e) {
            assertEquals("Pedido já possui entrega associada.", e.getMessage());
        }
    }

    // F) atribuirEntregador sem entregadores disponíveis deve falhar
    @Test
    @DisplayName("F: Atribuir entregador sem entregadores disponíveis deve falhar.")
    void atribuirEntregadorSemEntregadoresDisponiveis() {
        Pedido pedido = pedidoComEstado(30L, EstadoPedido.PRONTO_PARA_ENTREGA);

        Entregador indisponivel = entregador(700L, false);
        Entregador ocupado = entregador(701L, true);

        when(pedidoRepository.findById(30L)).thenReturn(Optional.of(pedido));
        when(entregaRepository.findByPedidoId(30L)).thenReturn(Optional.empty());
        when(utilizadorRepository.findAll()).thenReturn(List.of(indisponivel, ocupado));
        when(entregaRepository.existsByEntregadorIdAndHoraEntregaIsNull(701L)).thenReturn(true);

        try {
            entregaService.atribuirEntregador(new AtribuirEntregadorDTO(30L));
            fail("Não há entregadores disponíveis.");
        } catch (IllegalStateException e) {
            assertEquals("Não há entregadores disponíveis.", e.getMessage());
        }
    }

    // G/H) atribuirEntregador com sucesso deve marcar entregador indisponível e mudar pedido
    @Test
    @DisplayName("G/H: Atribuir entregador com sucesso deve marcar entregador indisponível e mover pedido para ATRIBUIDO_ENTREGADOR.")
    void atribuirEntregadorSuccess() {
        Pedido pedido = pedidoComEstado(40L, EstadoPedido.PRONTO_PARA_ENTREGA);
        Entregador livre = entregador(800L, true);

        when(pedidoRepository.findById(40L)).thenReturn(Optional.of(pedido));
        when(entregaRepository.findByPedidoId(40L)).thenReturn(Optional.empty());
        when(utilizadorRepository.findAll()).thenReturn(List.of(livre));
        when(entregaRepository.existsByEntregadorIdAndHoraEntregaIsNull(800L)).thenReturn(false);
        when(utilizadorRepository.save(any(Entregador.class))).thenAnswer(i -> i.getArgument(0));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(i -> i.getArgument(0));
        when(entregaRepository.save(any(Entrega.class))).thenAnswer(i -> i.getArgument(0));

        Entrega entrega = entregaService.atribuirEntregador(new AtribuirEntregadorDTO(40L));

        assertEquals(800L, entrega.getEntregadorId());
        assertFalse(livre.isAvailable());
        assertEquals(EstadoPedido.ATRIBUIDO_ENTREGADOR, pedido.getEstado());
        verify(utilizadorRepository).save(livre);
        verify(pedidoRepository).save(pedido);
        verify(entregaRepository).save(any(Entrega.class));
    }

    // I) iniciarPreparacao com pedido inexistente deve falhar
    @Test
    @DisplayName("I: Iniciar preparação com pedido inexistente deve falhar.")
    void iniciarPreparacaoWithPedidoInexistente() {
        when(pedidoRepository.findById(55L)).thenReturn(Optional.empty());

        try {
            entregaService.iniciarPreparacao(55L);
            fail("Pedido inexistente.");
        } catch (IllegalArgumentException e) {
            assertEquals("Pedido não encontrado: 55", e.getMessage());
        }
    }

    // J) iniciarPreparacao com estado inválido deve falhar
    @Test
    @DisplayName("J: Iniciar preparação com estado inválido deve falhar.")
    void iniciarPreparacaoWithEstadoInvalido() {
        Pedido pedido = pedidoComEstado(56L, EstadoPedido.CRIADO);
        when(pedidoRepository.findById(56L)).thenReturn(Optional.of(pedido));

        try {
            entregaService.iniciarPreparacao(56L);
            fail("Estado inválido para iniciar preparação.");
        } catch (IllegalStateException e) {
            assertEquals("O pedido precisa de ser pago antes de ser preparado.", e.getMessage());
        }
    }

    // K) iniciarPreparacao com estado PAGO deve passar
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

    // L) marcarPedidoComoPronto com estado inválido deve falhar
    @Test
    @DisplayName("L: Marcar pedido como pronto com estado inválido deve falhar.")
    void marcarPedidoComoProntoWithEstadoInvalido() {
        Pedido pedido = pedidoComEstado(60L, EstadoPedido.PAGO);
        when(pedidoRepository.findById(60L)).thenReturn(Optional.of(pedido));

        try {
            entregaService.marcarPedidoComoPronto(60L);
            fail("Estado inválido para marcar como pronto.");
        } catch (IllegalStateException e) {
            assertEquals("O pedido não estava em preparação.", e.getMessage());
        }
    }

    // M) marcarPedidoComoPronto com estado A_PREPARAR deve passar
    @Test
    @DisplayName("M: Marcar pedido como pronto com estado A_PREPARAR deve passar.")
    void marcarPedidoComoProntoComAPreparar() {
        Pedido pedido = pedidoComEstado(61L, EstadoPedido.A_PREPARAR);
        when(pedidoRepository.findById(61L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(i -> i.getArgument(0));

        Pedido atualizado = entregaService.marcarPedidoComoPronto(61L);

        assertEquals(EstadoPedido.PRONTO_PARA_ENTREGA, atualizado.getEstado());
        verify(pedidoRepository).save(pedido);
    }

    // N) registarRecolha sem entrega deve falhar
    @Test
    @DisplayName("N: Registar recolha sem entrega deve falhar.")
    void registarRecolhaSemEntrega() {
        when(entregaRepository.findByPedidoId(70L)).thenReturn(Optional.empty());

        try {
            entregaService.registarRecolha(70L);
            fail("Entrega não encontrada para pedido.");
        } catch (IllegalArgumentException e) {
            assertEquals("Entrega não encontrada para pedido: 70", e.getMessage());
        }
    }

    // O) registarRecolha com pedido fora de ATRIBUIDO_ENTREGADOR deve falhar
    @Test
    @DisplayName("O: Registar recolha com pedido fora de ATRIBUIDO_ENTREGADOR deve falhar.")
    void registarRecolhaComEstadoInvalido() {
        Pedido pedido = pedidoComEstado(71L, EstadoPedido.PRONTO_PARA_ENTREGA);
        Entrega entrega = entregaParaPedido(pedido, 900L);

        when(entregaRepository.findByPedidoId(71L)).thenReturn(Optional.of(entrega));
        when(pedidoRepository.findById(71L)).thenReturn(Optional.of(pedido));

        try {
            entregaService.registarRecolha(71L);
            fail("Só é possível registar recolha após atribuição de entregador.");
        } catch (IllegalStateException e) {
            assertEquals("Só é possível registar recolha após atribuição de entregador.", e.getMessage());
        }
    }

    // P) registarRecolha com sucesso deve definir horaRecolha
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

    // Q) registarEntregaFinal sem entrega deve falhar
    @Test
    @DisplayName("Q: Registar entrega final sem entrega deve falhar.")
    void registarEntregaFinalSemEntrega() {
        when(entregaRepository.findByPedidoId(80L)).thenReturn(Optional.empty());

        try {
            entregaService.registarEntregaFinal(80L);
            fail("Entrega não encontrada para pedido.");
        } catch (IllegalArgumentException e) {
            assertEquals("Entrega não encontrada para pedido: 80", e.getMessage());
        }
    }

    // R) registarEntregaFinal sem entregador válido deve falhar
    @Test
    @DisplayName("R: Registar entrega final sem entregador válido deve falhar.")
    void registarEntregaFinalSemEntregadorValido() {
        Pedido pedido = pedidoComEstado(81L, EstadoPedido.ATRIBUIDO_ENTREGADOR);
        Entrega entrega = entregaParaPedido(pedido, 990L);
        entrega.setHoraRecolha(java.time.LocalDateTime.now());

        when(entregaRepository.findByPedidoId(81L)).thenReturn(Optional.of(entrega));
        when(pedidoRepository.findById(81L)).thenReturn(Optional.of(pedido));
        when(utilizadorRepository.findById(990L)).thenReturn(Optional.empty());

        try {
            entregaService.registarEntregaFinal(81L);
            fail("Entregador não encontrado.");
        } catch (IllegalArgumentException e) {
            assertEquals("Entregador não encontrado: 990", e.getMessage());
        }

        verify(utilizadorRepository, never()).save(any());
        verify(pedidoRepository, never()).save(any());
        verify(entregaRepository, never()).save(any());
    }

    // S/T/U) registarEntregaFinal com sucesso define horaEntrega, muda estado e liberta entregador
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

    private Pedido pedidoComEstado(Long id, EstadoPedido estado) {
        Pedido pedido = new Pedido(id, 1L, 2L);
        pedido.setEstado(estado);
        return pedido;
    }

    private Entregador entregador(Long id, boolean available) {
        Entregador entregador = new Entregador("Driver", "driver" + id + "@tasca.pt", "pw");
        entregador.setId(id);
        entregador.setAvailable(available);
        return entregador;
    }

    private Entrega entregaParaPedido(Pedido pedido, Long entregadorId) {
        return new Entrega(pedido, entregadorId, "Rua Teste 1");
    }

    @SuppressWarnings("unused")
    private Cliente cliente(Long id, String email) {
        Cliente cliente = new Cliente();
        cliente.setId(id);
        cliente.setEmail(email);
        cliente.setName("Cliente");
        cliente.setPassword("pw");
        return cliente;
    }
}
