package pt.ul.fc.css.tascaeats.dto.utilizadorDTO;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import pt.ul.fc.css.tascaeats.dto.moradaDTO.MoradaDTO;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public MoradaDTO getAddress() {
        return address;
    }

    public void setAddress(MoradaDTO address) {
        this.address = address;
    }
}
