package pt.ul.fc.css.delivery.dto.entregaDTO;

import java.time.LocalDateTime;

import pt.ul.fc.css.delivery.domain.entrega.EstadoEntrega;

public class EntregaResumoDTO {

    private Long id;
    private Long pedidoId;
    private Long entregadorId;
    private EstadoEntrega estado;
    private LocalDateTime horaRecolha;
    private LocalDateTime horaEntrega;
    private String enderecoEntrega;

    //********MÉTODOS*********

    public EntregaResumoDTO() {
    }

    public EntregaResumoDTO(
            Long id,
            Long pedidoId,
            Long entregadorId,
            EstadoEntrega estado,
            LocalDateTime horaRecolha,
            LocalDateTime horaEntrega,
            String enderecoEntrega) {
        this.id = id;
        this.pedidoId = pedidoId;
        this.entregadorId = entregadorId;
        this.estado = estado;
        this.horaRecolha = horaRecolha;
        this.horaEntrega = horaEntrega;
        this.enderecoEntrega = enderecoEntrega;
    }


    //********GETTERS E SETTERS*********

    public Long getId() {return id;}

    public void setId(Long id) {this.id = id;}

    public Long getPedidoId() {return pedidoId;}

    public void setPedidoId(Long pedidoId) {this.pedidoId = pedidoId;}

    public Long getEntregadorId() {return entregadorId;}

    public void setEntregadorId(Long entregadorId) {this.entregadorId = entregadorId;}

    public EstadoEntrega getEstado() {return estado;}

    public void setEstado(EstadoEntrega estado) {this.estado = estado;}

    public LocalDateTime getHoraRecolha() {return horaRecolha;}

    public void setHoraRecolha(LocalDateTime horaRecolha) {this.horaRecolha = horaRecolha;}

    public LocalDateTime getHoraEntrega() {return horaEntrega;}

    public void setHoraEntrega(LocalDateTime horaEntrega) {this.horaEntrega = horaEntrega;}

    public String getEnderecoEntrega() {return enderecoEntrega;}

    public void setEnderecoEntrega(String enderecoEntrega) {this.enderecoEntrega = enderecoEntrega;}
}
