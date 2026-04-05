package pt.ul.fc.css.tascaeats.dto;

import jakarta.validation.constraints.NotNull;
import pt.ul.fc.css.tascaeats.domain.EstadoRestaurante;

public class AtualizarEstadoRestauranteDTO{

    @NotNull
    private EstadoRestaurante estado;

    public EstadoRestaurante getEstado(){
        return estado;
    }

    public void setEstado(EstadoRestaurante estado){
        this.estado = estado;
    }

}