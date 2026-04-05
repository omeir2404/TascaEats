package pt.ul.fc.css.tascaeats.domain;

import java.time.Duration;
import java.time.LocalDateTime;

public class Entrega {


    private Long id;

    private LocalDateTime horaRecolha;
    private LocalDateTime horaEntrega;
    private String enderecoEntrega;
    private Long entregadorId;

    private Pedido pedido;

    protected Entrega(){}



    public Entrega(Pedido pedido, Long entregadorid, String enderecoEntrega) {
        this.pedido = pedido;
        this.entregadorId = entregadorid;
        this.enderecoEntrega = enderecoEntrega;
    }

// ################### MÉTODOS DE NEGÓCIO ######################################################################

    public void registarRecolha() {
        if (this.horaRecolha != null) {
            throw new IllegalStateException("A recolha deste pedido já foi registada.");
        }
        this.horaRecolha = LocalDateTime.now();
    }

    public void registarConclusao() {
        if (this.horaRecolha == null) {
            throw new IllegalStateException("Não é possível registar a entrega de um pedido que ainda não foi recolhido.");
        }
        
        if (this.horaEntrega != null) {
            throw new IllegalStateException("A entrega deste pedido já foi concluída anteriormente.");
        }
        
        this.horaEntrega = LocalDateTime.now();
    }

    public Duration calcularTempoDeViagem() {
        return Duration.between(this.horaRecolha, this.horaEntrega);
    }

// ###################### GETTERS e SETTERS #####################################################################

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
