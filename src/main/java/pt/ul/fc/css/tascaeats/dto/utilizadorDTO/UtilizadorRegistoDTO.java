package pt.ul.fc.css.tascaeats.dto.utilizadorDTO;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UtilizadorRegistoDTO {
    String name;
    @Email(message = "Email should be valid")
    String email;
    String password;
    String type;
    @Nullable
    MoradaDTO address;

    public UtilizadorRegistoDTO(@NotNull String name, @NotNull String email, @NotNull String password, @NotNull String type, @Nullable MoradaDTO address) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.type = type;
        this.address = address;
    }
}
