package pt.ul.fc.css.tascaeats.dto;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO para processar pagamentos em pedidos.
 * 
 * Exemplo de uso:
 * {
 *   "pedidoId": 1,
 *   "tipoPagamento": "MBWAY",
 *   "detalhes": {
 *     "numeroTelemovel": "912345678"
 *   }
 * }
 */
public class ProcessarPagamentoDTO {

    @NotNull(message = "Pedido ID é obrigatório")
    @Positive(message = "Pedido ID deve ser maior que 0")
    private Long pedidoId;

    @NotBlank(message = "Tipo de pagamento é obrigatório")
    private String tipoPagamento;

    @NotNull(message = "Detalhes do pagamento são obrigatórios")
    @NotEmpty(message = "Detalhes do pagamento não podem estar vazios")
    private Map<String, Object> detalhes;

    public ProcessarPagamentoDTO() {
    }

    public ProcessarPagamentoDTO(Long pedidoId, String tipoPagamento, Map<String, Object> detalhes) {
        this.pedidoId = pedidoId;
        this.tipoPagamento = tipoPagamento;
        this.detalhes = detalhes;
    }

    // ###################### GETTERS e SETTERS #####################################################################

    public Long getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
    }

    public String getTipoPagamento() {
        return tipoPagamento;
    }

    public void setTipoPagamento(String tipoPagamento) {
        this.tipoPagamento = tipoPagamento;
    }

    public Map<String, Object> getDetalhes() {
        return detalhes;
    }

    public void setDetalhes(Map<String, Object> detalhes) {
        this.detalhes = detalhes;
    }

    @Override
    public String toString() {
        return "ProcessarPagamentoDTO{" + "pedidoId=" + pedidoId + ", tipoPagamento='" + 
        tipoPagamento + '\'' + ", detalhes=" + detalhes + '}';
    }
}
