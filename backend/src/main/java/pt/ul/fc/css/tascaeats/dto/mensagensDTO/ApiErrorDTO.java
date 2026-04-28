// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.dto.mensagensDTO;

public class ApiErrorDTO {
    private String error;

    public ApiErrorDTO() {
    }

    public ApiErrorDTO(String error) {
        this.error = error;
    }

    // ###################### GETTERS e SETTERS #####################################################################

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}

