// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################

package pt.ul.fc.css.delivery.dto.entregadorDTO;

public class AtualizarEntregadorDTO {

    private String nome;
    private String email;
    private Boolean available;

    public AtualizarEntregadorDTO() {
    }

    public AtualizarEntregadorDTO(String nome, String email, Boolean available) {
        this.nome = nome;
        this.email = email;
        this.available = available;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }
}
