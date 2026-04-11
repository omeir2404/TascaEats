// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.dto.pedidoDTO;

import jakarta.validation.constraints.NotBlank;


public class AtualizarEstadoPedidoDTO {


    @NotBlank(message = "Novo estado é obrigatório")
    private String novoEstado;


    public AtualizarEstadoPedidoDTO(){}

    public AtualizarEstadoPedidoDTO(String novoEstado){
        this.novoEstado = novoEstado;
    }

    
// ###################### GETTERS e SETTERS #####################################################################

    public String getNovoEstado() {
        return novoEstado;
    }

    public void setNovoEstado(String novoEstado) {
        this.novoEstado = novoEstado;
    }


    @Override
    public String toString(){
        return "AtualizarEstadoPedidoDTO{" + "novoEstado=" + novoEstado +"}";
    }
}
