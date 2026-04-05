package pt.ul.fc.css.tascaeats.domain.DTOs;

import lombok.Getter;
import lombok.Setter;

public class UserRegistrationDTO {

    @Getter
    @Setter
    private String email;
    @Getter
    @Setter
    private String password;
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private String type;

    public UserRegistrationDTO(String email, String password, String name, String type) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.type = type;
    }

}
