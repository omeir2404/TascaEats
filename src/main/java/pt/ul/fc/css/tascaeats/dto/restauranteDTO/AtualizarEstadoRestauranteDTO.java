package pt.ul.fc.css.tascaeats.dto.restauranteDTO;

import jakarta.validation.constraints.NotNull;
import pt.ul.fc.css.tascaeats.domain.EstadoRestaurante;

public class AtualizarEstadoRestauranteDTO{

    @NotNull
    private EstadoRestaurante estado;


    // *********GETTERS E SETTERS**********

    public EstadoRestaurante getEstado(){
        return estado;
    }

    public void setEstado(EstadoRestaurante estado){
        this.estado = estado;
    }

}