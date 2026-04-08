package pt.ul.fc.css.tascaeats.dto.mensagensDTO;

public class ApiErrorDTO {
    private String error;

    public ApiErrorDTO() {
    }

    public ApiErrorDTO(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}

