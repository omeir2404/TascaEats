package pt.ul.fc.css.delivery.dto.entregaDTO;

import pt.ul.fc.css.delivery.domain.entrega.EstadoEntrega;

public class AtualizarEstadoEntregaDTO {

    private EstadoEntrega estado;

    public AtualizarEstadoEntregaDTO() {
    }

    public AtualizarEstadoEntregaDTO(EstadoEntrega estado) {
        this.estado = estado;
    }

    public EstadoEntrega getEstado() {
        return estado;
    }

    public void setEstado(EstadoEntrega estado) {
        this.estado = estado;
    }
}
