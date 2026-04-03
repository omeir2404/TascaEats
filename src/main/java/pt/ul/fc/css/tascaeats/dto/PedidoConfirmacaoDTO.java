package pt.ul.fc.css.tascaeats.dto;

import java.time.LocalDateTime;

public class PedidoConfirmacaoDTO {

    private Long id;
    private double valorTotal;
    private String estado;
    private LocalDateTime dataHora;

    public PedidoConfirmacaoDTO() {
    }

    public PedidoConfirmacaoDTO(Long id, double valorTotal, String estado, LocalDateTime dataHora) {
        this.id = id;
        this.valorTotal = valorTotal;
        this.estado = estado;
        this.dataHora = dataHora;
    }


}
