package pt.ul.fc.css.tascaeats.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserUpdateDTO {
    String name;
    @Email(message = "Email should be valid")
    String email;
    String password;
    String type;
    @Nullable
    String address;
    boolean available;

    public UserUpdateDTO(@NotNull String name, @NotNull String email, @NotNull String password, @NotNull String type, String address, boolean available) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.type = type;
        this.address = address;
        this.available = available;
    }

}
