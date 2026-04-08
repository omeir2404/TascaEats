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

// ###################### GETTERS e SETTERS #####################################################################

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(double valorTotal) {
        this.valorTotal = valorTotal;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    @Override
    public String toString(){
        return "PedidoConfirmacaoDTO{" + "id=" + id + ", valorTotal=" 
            + valorTotal + ", estado=" + estado + ", dataHora=" + dataHora +"}";
    }
}
