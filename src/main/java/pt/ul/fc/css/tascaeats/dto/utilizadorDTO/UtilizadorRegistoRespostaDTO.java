package pt.ul.fc.css.tascaeats.dto.utilizadorDTO;

public class UtilizadorRegistoRespostaDTO {
    private String message;
    private Long id;
    private String email;
    private String type;

    public UtilizadorRegistoRespostaDTO() {
    }

    public UtilizadorRegistoRespostaDTO(String message, Long id, String email, String type) {
        this.message = message;
        this.id = id;
        this.email = email;
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

