package pt.ul.fc.css.tascaeats.dto;

import java.time.LocalDateTime;

public class EntregaResumoDTO {

    private Long id;
    private LocalDateTime horaRecolha;
    private LocalDateTime horaEntrega;
    private String entregadorNome;

    public EntregaResumoDTO() {
    }

    public EntregaResumoDTO(Long id, LocalDateTime horaRecolha, LocalDateTime horaEntrega, String entregadorNome) {
        this.id = id;
        this.horaRecolha = horaRecolha;
        this.horaEntrega = horaEntrega;
        this.entregadorNome = entregadorNome;
    }

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

    public String getEntregadorNome() {
        return entregadorNome;
    }

    public void setEntregadorNome(String entregadorNome) {
        this.entregadorNome = entregadorNome;
    }
}
