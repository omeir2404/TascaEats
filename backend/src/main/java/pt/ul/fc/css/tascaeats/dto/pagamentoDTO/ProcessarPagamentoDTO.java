    // ##############################################################
    // 						  TASCAEATS								#
    // 						CSS GRUPO: 25							#
    //           Autores:                Numero Aluno:              #
    //        - Omeir Haroon               61810                    #
    //        - Isa David                  61871                    #
    //        - Diogo Roque                61857                    #
    // ##############################################################
    package pt.ul.fc.css.tascaeats.dto.pagamentoDTO;

    import java.util.Map;

    import jakarta.validation.constraints.NotBlank;
    import jakarta.validation.constraints.NotEmpty;
    import jakarta.validation.constraints.NotNull;
    import jakarta.validation.constraints.Positive;
    import jakarta.validation.constraints.PositiveOrZero;

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

        private String bandeiraCartao;

        private String numeroMbway;

        @PositiveOrZero
        private Double valorTroco;

        @NotNull(message = "Detalhes do pagamento são obrigatórios")
        @NotEmpty(message = "Detalhes do pagamento não podem estar vazios")
        private Map<String, Object> detalhes;

        // *********CONSTRUTORES**********

        public ProcessarPagamentoDTO() {
        }

        public ProcessarPagamentoDTO(Long pedidoId, String tipoPagamento, Map<String, Object> detalhes) {
            this(pedidoId, tipoPagamento, detalhes, null, null, null);
        }

        public ProcessarPagamentoDTO(Long pedidoId, String tipoPagamento, Map<String, Object> detalhes,
                                     String bandeiraCartao, String numeroMbway, Double valorTroco) {
            this.pedidoId = pedidoId;
            this.tipoPagamento = tipoPagamento;
            this.detalhes = detalhes;
            this.bandeiraCartao = bandeiraCartao;
            this.numeroMbway = numeroMbway;
            this.valorTroco = valorTroco;
        }

        public ProcessarPagamentoDTO(Long pedidoId, String tipoPagamento, String bandeiraCartao, String numeroMbway,
                                     Double valorTroco, Map<String, Object> detalhes) {
            this(pedidoId, tipoPagamento, detalhes, bandeiraCartao, numeroMbway, valorTroco);
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

        public String getBandeiraCartao() { return bandeiraCartao; }

        public void setBandeiraCartao(String bandeiraCartao) { this.bandeiraCartao = bandeiraCartao; }

        public String getNumeroMbway() { return numeroMbway; }

        public void setNumeroMbway(String numeroMbway) { this.numeroMbway = numeroMbway; }

        public Double getValorTroco() { return valorTroco; }

        public void setValorTroco(Double valorTroco) { this.valorTroco = valorTroco; }

        public Map<String, Object> getDetalhes() {
            return detalhes;
        }

        public void setDetalhes(Map<String, Object> detalhes) {
            this.detalhes = detalhes;
        }

        @Override
        public String toString() {
            return "ProcessarPagamentoDTO{" + "pedidoId=" + pedidoId + ", tipoPagamento='" + tipoPagamento + '\'' +
                    ", bandeiraCartao='" + bandeiraCartao + '\'' + ", numeroMbway='" + numeroMbway + '\'' +
                    ", valorTroco=" + valorTroco + ", detalhes=" + detalhes + '}';
        }
    }
