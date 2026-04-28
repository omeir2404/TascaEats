// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.dto.mensagensDTO;

public class ApiMessageDTO {
    private String message;

    public ApiMessageDTO() {
    }

    public ApiMessageDTO(String message) {
        this.message = message;
    }

    // ###################### GETTERS e SETTERS #####################################################################

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

