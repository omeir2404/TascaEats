// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pt.ul.fc.css.tascaeats.domain.pedido.EstadoPedido;
import pt.ul.fc.css.tascaeats.domain.pedido.Pedido;
import pt.ul.fc.css.tascaeats.dto.pedidoDTO.CriarPedidoDTO;
import pt.ul.fc.css.tascaeats.service.pedidoService.PedidoService;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PedidoController.class)
@SuppressWarnings("null")
class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PedidoService pedidoService;

    @Test
    @DisplayName("A: POST /pedidos com payload válido deve retornar 200 e mensagem de sucesso.")
    void shouldCreatePedidoWhenPayloadIsValid() throws Exception {
        CriarPedidoDTO dto = new CriarPedidoDTO(1L, 2L, List.of(10L, 11L));

        mockMvc.perform(post("/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Pedido criado com sucesso."));

        verify(pedidoService).criarPedido(any(CriarPedidoDTO.class));
    }

    @Test
    @DisplayName("A1: POST /pedidos com texto JSON válido deve criar pedido.")
    void shouldCreatePedidoWhenRawJsonTextIsValid() throws Exception {
        String rawJson = objectMapper.writeValueAsString(new CriarPedidoDTO(1L, 2L, List.of(10L, 11L)));

        mockMvc.perform(post("/pedidos")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(rawJson))
                .andExpect(status().isOk())
                .andExpect(content().string("Pedido criado com sucesso."));

        verify(pedidoService).criarPedido(any(CriarPedidoDTO.class));
    }

    @Test
    @DisplayName("B: POST /pedidos com payload inválido deve retornar erro HTTP apropriado.")
    void shouldReturnBadRequestWhenPayloadIsInvalid() throws Exception {
        Map<String, Object> payloadInvalido = Map.of(
                "restauranteId", 2,
                "produtosIds", List.of(10, 11)
        );

        mockMvc.perform(post("/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payloadInvalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("B1: POST /pedidos com texto inválido deve retornar 400.")
    void shouldReturnBadRequestWhenRawTextPayloadIsInvalid() throws Exception {
        mockMvc.perform(post("/pedidos")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("{ invalid json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("B2: POST /pedidos deve rejeitar utilizador que não seja cliente.")
    void shouldReturnBadRequestWhenOrderCreatorIsNotCliente() throws Exception {
        CriarPedidoDTO dto = new CriarPedidoDTO(99L, 2L, List.of(10L, 11L));

        doThrow(new IllegalArgumentException("Apenas clientes podem criar pedidos."))
                .when(pedidoService).criarPedido(any(CriarPedidoDTO.class));

        mockMvc.perform(post("/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Apenas clientes podem criar pedidos."));
    }

    @Test
    @DisplayName("C: PATCH /pedidos/{id}/cancelar com sucesso deve retornar 200.")
    void shouldCancelPedidoWhenRequestIsValid() throws Exception {
        mockMvc.perform(patch("/pedidos/50/cancelar"))
                .andExpect(status().isOk())
                .andExpect(content().string("Pedido cancelado com sucesso."));

        verify(pedidoService).cancelarPedido(50L);
    }

    @Test
    @DisplayName("D: PATCH /pedidos/{id}/cancelar com erro de negócio deve retornar 4xx/5xx conforme handler.")
    void shouldPropagateBusinessErrorWhenCancellingPedido() {
        doThrow(new IllegalStateException("Pedido no estado A_PREPARAR não pode ser cancelado."))
            .when(pedidoService).cancelarPedido(51L);

        ServletException ex = assertThrows(
                ServletException.class,
                () -> mockMvc.perform(patch("/pedidos/51/cancelar"))
        );

        assertTrue(ex.getMessage().contains("Pedido no estado A_PREPARAR não pode ser cancelado."));
    }

    @Test
    @DisplayName("E: GET /pedidos sem filtros deve retornar lista.")
    void shouldListPedidosWithoutFilters() throws Exception {
        Pedido p1 = pedido(100L, 1L, EstadoPedido.CRIADO);
        Pedido p2 = pedido(101L, 2L, EstadoPedido.PAGO);

        when(pedidoService.listarPedidos(null, null)).thenReturn(List.of(p1, p2));

        mockMvc.perform(get("/pedidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100))
                .andExpect(jsonPath("$[1].id").value(101));

        verify(pedidoService).listarPedidos(null, null);
    }

    @Test
    @DisplayName("F: GET /pedidos com clienteId deve filtrar.")
    void shouldListPedidosByClienteId() throws Exception {
        Pedido p = pedido(102L, 9L, EstadoPedido.CRIADO);
        when(pedidoService.listarPedidos(9L, null)).thenReturn(List.of(p));

        mockMvc.perform(get("/pedidos").param("clienteId", "9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].clienteId").value(9));

        verify(pedidoService).listarPedidos(9L, null);
    }

    @Test
    @DisplayName("G: GET /pedidos com estado deve filtrar.")
    void shouldListPedidosByEstado() throws Exception {
        Pedido p = pedido(103L, 3L, EstadoPedido.PAGO);
        when(pedidoService.listarPedidos(null, EstadoPedido.PAGO)).thenReturn(List.of(p));

        mockMvc.perform(get("/pedidos").param("estado", "PAGO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estado").value("PAGO"));

        verify(pedidoService).listarPedidos(null, EstadoPedido.PAGO);
    }

    @Test
    @DisplayName("H: GET /pedidos com ambos filtros deve filtrar corretamente.")
    void shouldListPedidosByClienteIdAndEstado() throws Exception {
        Pedido p = pedido(104L, 7L, EstadoPedido.CANCELADO);
        when(pedidoService.listarPedidos(7L, EstadoPedido.CANCELADO)).thenReturn(List.of(p));

        mockMvc.perform(get("/pedidos")
                        .param("clienteId", "7")
                        .param("estado", "CANCELADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].clienteId").value(7))
                .andExpect(jsonPath("$[0].estado").value("CANCELADO"));

        verify(pedidoService).listarPedidos(7L, EstadoPedido.CANCELADO);
    }

    private Pedido pedido(Long id, Long clienteId, EstadoPedido estado) {
        Pedido pedido = new Pedido(id, clienteId, 2L);
        pedido.setEstado(estado);
        return pedido;
    }
}
