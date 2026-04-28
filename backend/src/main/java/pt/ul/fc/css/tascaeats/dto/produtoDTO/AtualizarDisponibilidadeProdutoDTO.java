// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.dto.produtoDTO;

import jakarta.validation.constraints.NotNull;

public class AtualizarDisponibilidadeProdutoDTO{
    
    @NotNull
    private Boolean disponivel;


    // ###################### GETTERS e SETTERS #####################################################################

    public Boolean getDisponivel(){
        return disponivel;
    }

    public void setDisponivel(Boolean disponivel){
        this.disponivel = disponivel;
    }
}
