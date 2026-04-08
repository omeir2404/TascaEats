package pt.ul.fc.css.tascaeats.dto.mensagensDTO;

public class ApiMessageDTO {
    private String message;

    public ApiMessageDTO() {
    }

    public ApiMessageDTO(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

