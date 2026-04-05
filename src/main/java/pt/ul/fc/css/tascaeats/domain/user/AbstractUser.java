package pt.ul.fc.css.tascaeats.domain.user;

import jakarta.persistence.GeneratedValue;
import org.springframework.data.annotation.Id;

public abstract class AbstractUser {
    @Id
    @GeneratedValue
    String id;

    String name;
    String email;
    String password;

    public AbstractUser(String name, String email, String password, Address address) {
        this.name = name;
        this.email = email;
        this.password = password;
    }
}
