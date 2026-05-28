package pt.ul.fc.css.delivery.dto.entregadorDTO;

public class EntregadorDTO {

    private Long id;
    private String nome;
    private String email;
    private boolean available;

    public EntregadorDTO() {
    }

    public EntregadorDTO(Long id, String nome, String email, boolean available) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.available = available;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
