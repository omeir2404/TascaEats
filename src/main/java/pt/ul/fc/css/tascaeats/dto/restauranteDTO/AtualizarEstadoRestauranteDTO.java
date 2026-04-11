// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.dto.restauranteDTO;

import jakarta.validation.constraints.NotNull;
import pt.ul.fc.css.tascaeats.domain.restaurante.EstadoRestaurante;

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