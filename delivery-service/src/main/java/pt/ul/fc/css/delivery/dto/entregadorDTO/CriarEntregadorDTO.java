package pt.ul.fc.css.delivery.dto.entregadorDTO;

public class CriarEntregadorDTO {

    private String nome;
    private String email;

    public CriarEntregadorDTO() {
    }

    public CriarEntregadorDTO(String nome, String email) {
        this.nome = nome;
        this.email = email;
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
}
