// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.controllers;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import pt.ul.fc.css.tascaeats.domain.entrega.Entrega;
import pt.ul.fc.css.tascaeats.domain.pedido.EstadoPedido;
import pt.ul.fc.css.tascaeats.domain.pedido.Pedido;
import pt.ul.fc.css.tascaeats.dto.entregaDTO.AtribuirEntregadorDTO;
import pt.ul.fc.css.tascaeats.service.entregaService.EntregaService;

@WebMvcTest(EntregaController.class)
@SuppressWarnings("null")
class EntregaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EntregaService entregaService;

    @Test
    @DisplayName("A: POST /entregas/atribuir com payload válido retorna resumo da entrega.")
    void shouldAssignDriverWhenPayloadIsValid() throws Exception {
        Pedido pedido = pedido(1L, EstadoPedido.ATRIBUIDO_ENTREGADOR);
        Entrega entrega = entrega(pedido, 900L);
        ReflectionTestUtils.setField(entrega, "id", 10L);

        when(entregaService.atribuirEntregador(any(AtribuirEntregadorDTO.class))).thenReturn(entrega);

        mockMvc.perform(post("/entregas/atribuir")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AtribuirEntregadorDTO(1L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.entregadorNome").value("900"));

        verify(entregaService).atribuirEntregador(any(AtribuirEntregadorDTO.class));
    }

    @Test
    @DisplayName("B: POST /entregas/atribuir com payload inválido retorna erro.")
    void shouldReturnBadRequestWhenAssignPayloadIsInvalid() throws Exception {
        Map<String, Object> payloadSemPedidoId = Map.of();

        mockMvc.perform(post("/entregas/atribuir")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payloadSemPedidoId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("C: PATCH /entregas/pedido/{id}/iniciar-preparacao com sucesso retorna 200.")
    void shouldStartPreparationWhenRequestIsValid() throws Exception {
        when(entregaService.iniciarPreparacao(30L)).thenReturn(pedido(30L, EstadoPedido.A_PREPARAR));

        mockMvc.perform(patch("/entregas/pedido/30/iniciar-preparacao"))
                .andExpect(status().isOk())
                .andExpect(content().string("Pedido 30 em preparação."));
    }

    @Test
    @DisplayName("D: PATCH /entregas/pedido/{id}/marcar-pronto com sucesso retorna 200.")
    void shouldMarkOrderReadyWhenRequestIsValid() throws Exception {
        when(entregaService.marcarPedidoComoPronto(31L)).thenReturn(pedido(31L, EstadoPedido.PRONTO_PARA_ENTREGA));

        mockMvc.perform(patch("/entregas/pedido/31/marcar-pronto"))
                .andExpect(status().isOk())
                .andExpect(content().string("Pedido 31 pronto para entrega."));
    }

    @Test
    @DisplayName("E: PATCH /entregas/pedido/{id}/recolha com sucesso retorna resumo com horaRecolha.")
    void shouldRegisterPickupWhenRequestIsValid() throws Exception {
        Pedido pedido = pedido(32L, EstadoPedido.ATRIBUIDO_ENTREGADOR);
        Entrega entrega = entrega(pedido, 901L);
        ReflectionTestUtils.setField(entrega, "id", 11L);
        entrega.setHoraRecolha(LocalDateTime.of(2026, 4, 11, 18, 30));

        when(entregaService.registarRecolha(32L)).thenReturn(entrega);

        mockMvc.perform(patch("/entregas/pedido/32/recolha"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(11))
                .andExpect(jsonPath("$.horaRecolha").value("2026-04-11T18:30:00"));
    }

    @Test
    @DisplayName("F: PATCH /entregas/pedido/{id}/entrega-final com sucesso retorna resumo com horaEntrega.")
    void shouldRegisterFinalDeliveryWhenRequestIsValid() throws Exception {
        Pedido pedido = pedido(33L, EstadoPedido.ENTREGUE);
        Entrega entrega = entrega(pedido, 902L);
        ReflectionTestUtils.setField(entrega, "id", 12L);
        entrega.setHoraRecolha(LocalDateTime.of(2026, 4, 11, 18, 10));
        entrega.setHoraEntrega(LocalDateTime.of(2026, 4, 11, 18, 55));

        when(entregaService.registarEntregaFinal(33L)).thenReturn(entrega);

        mockMvc.perform(patch("/entregas/pedido/33/entrega-final"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(12))
                .andExpect(jsonPath("$.horaEntrega").value("2026-04-11T18:55:00"));
    }

    @Test
    @DisplayName("G: endpoints de entrega devem propagar erros de negócio corretamente.")
    void shouldPropagateBusinessErrorsOnDeliveryEndpoints() throws Exception {
        doThrow(new IllegalStateException("Pedido não está pronto para entrega."))
                .when(entregaService).atribuirEntregador(any(AtribuirEntregadorDTO.class));

        mockMvc.perform(post("/entregas/atribuir")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AtribuirEntregadorDTO(40L))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Pedido não está pronto para entrega."));

        when(entregaService.iniciarPreparacao(eq(41L)))
                .thenThrow(new IllegalStateException("O pedido precisa de ser pago antes de ser preparado."));

            ServletException ex = assertThrows(
            ServletException.class,
            () -> mockMvc.perform(patch("/entregas/pedido/41/iniciar-preparacao"))
        );
            assertTrue(ex.getMessage().contains("O pedido precisa de ser pago antes de ser preparado."));
    }

    private Pedido pedido(Long id, EstadoPedido estado) {
        Pedido pedido = new Pedido(id, 1L, 2L);
        pedido.setEstado(estado);
        return pedido;
    }

    private Entrega entrega(Pedido pedido, Long entregadorId) {
        return new Entrega(pedido, entregadorId, "Rua Teste 1");
    }
}
