// ##############################################################
// 					  TASCAEATS						#
// 					CSS GRUPO: 25						#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
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
