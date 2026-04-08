package pt.ul.fc.css.tascaeats.dto;

import lombok.Getter;
import lombok.Setter;

public class UserLoginDTO {

    @Getter
    @Setter
    private String email;
    @Getter
    @Setter
    private String password;


    public UserLoginDTO(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
