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


}
