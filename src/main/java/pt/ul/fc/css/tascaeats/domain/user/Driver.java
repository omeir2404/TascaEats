package pt.ul.fc.css.tascaeats.domain.user;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
public class Driver extends User {

    @Getter
    @Setter
    boolean available;

     public Driver(String name, String email, String password) {
        super(name, email, password);
    }

    public Driver() {

    }
}
