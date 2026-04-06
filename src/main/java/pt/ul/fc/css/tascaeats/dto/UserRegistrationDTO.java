package pt.ul.fc.css.tascaeats.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

public class UserRegistrationDTO {
    @Getter
    @Setter
    String name;
    @Getter
    @Setter
    @Email(message = "Email should be valid")
    String email;
    @Getter
    @Setter
    String password;
    @Getter
    @Setter
    String type;
    @Getter
    @Setter
    @Nullable
    String address;

    public UserRegistrationDTO(@NotNull String name, @NotNull String email, @NotNull String password, @NotNull String type, @Nullable String address) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.type = type;
        this.address = address;
    }
}
