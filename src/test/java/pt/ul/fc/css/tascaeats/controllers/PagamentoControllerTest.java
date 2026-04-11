package pt.ul.fc.css.tascaeats.controllers;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import pt.ul.fc.css.tascaeats.dto.pagamentoDTO.ProcessarPagamentoDTO;
import pt.ul.fc.css.tascaeats.service.pagamentoServices.PagamentoService;

@WebMvcTest(PagamentoController.class)
@SuppressWarnings("null")
class PagamentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PagamentoService pagamentoService;

    @Test
    @DisplayName("A: POST /pagamentos com payload válido retorna 200 e mensagem de sucesso.")
    void shouldReturnOkWhenPayloadIsValid() throws Exception {
        ProcessarPagamentoDTO dto = pagamentoDtoValido();

        mockMvc.perform(post("/pagamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Pagamento processado com sucesso."));

        verify(pagamentoService).processarPagamento(any(ProcessarPagamentoDTO.class));
    }

    @Test
    @DisplayName("B: POST /pagamentos com pedidoId ausente/inválido retorna erro 400.")
    void shouldReturnBadRequestWhenPedidoIdIsMissingOrInvalid() throws Exception {
        Map<String, Object> payloadSemPedidoId = Map.of(
                "tipoPagamento", "DINHEIRO",
                "detalhes", Map.of("valorRecebido", 20.0)
        );

        Map<String, Object> payloadPedidoIdInvalido = Map.of(
                "pedidoId", 0,
                "tipoPagamento", "DINHEIRO",
                "detalhes", Map.of("valorRecebido", 20.0)
        );

        mockMvc.perform(post("/pagamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payloadSemPedidoId)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/pagamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payloadPedidoIdInvalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("C: POST /pagamentos com tipo de pagamento inválido retorna erro.")
    void shouldReturnBadRequestWhenTipoPagamentoIsInvalid() throws Exception {
        ProcessarPagamentoDTO dto = new ProcessarPagamentoDTO(
                1L,
                "TIPO_X",
                Map.of("detalhe", "valor")
        );

        doThrow(new IllegalArgumentException("Tipo de pagamento não suportado: TIPO_X"))
                .when(pagamentoService).processarPagamento(any(ProcessarPagamentoDTO.class));

        mockMvc.perform(post("/pagamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Tipo de pagamento não suportado: TIPO_X"));
    }

    @Test
    @DisplayName("D: POST /pagamentos deve delegar para PagamentoService.")
    void shouldDelegateToPagamentoService() throws Exception {
        ProcessarPagamentoDTO dto = pagamentoDtoValido();

        mockMvc.perform(post("/pagamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(pagamentoService).processarPagamento(any(ProcessarPagamentoDTO.class));
    }

    private ProcessarPagamentoDTO pagamentoDtoValido() {
        return new ProcessarPagamentoDTO(
                1L,
                "DINHEIRO",
                Map.of("valorRecebido", 20.0)
        );
    }
}
