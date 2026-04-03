package pt.ul.fc.css.tascaeats.domain;

import java.time.LocalDateTime;

public class Entrega {


    private Long id;

    private LocalDateTime horaRecolha;
    private LocalDateTime horaEntrega;
    private String enderecoEntrega;
    private Long entregadorId;

    private Pedido pedido;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getHoraRecolha() {
        return horaRecolha;
    }

    public void setHoraRecolha(LocalDateTime horaRecolha) {
        this.horaRecolha = horaRecolha;
    }

    public LocalDateTime getHoraEntrega() {
        return horaEntrega;
    }

    public void setHoraEntrega(LocalDateTime horaEntrega) {
        this.horaEntrega = horaEntrega;
    }

    public String getEnderecoEntrega() {
        return enderecoEntrega;
    }

    public void setEnderecoEntrega(String enderecoEntrega) {
        this.enderecoEntrega = enderecoEntrega;
    }

    public Long getEntregadorId() {
        return entregadorId;
    }

    public void setEntregadorId(Long entregadorId) {
        this.entregadorId = entregadorId;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }
}
