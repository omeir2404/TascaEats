// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.dto.utilizadorDTO;

public class UtilizadorLoginDTO {

    private String email;
    private String password;

    public UtilizadorLoginDTO() {
    }

    public UtilizadorLoginDTO(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // ###################### GETTERS e SETTERS #####################################################################

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
