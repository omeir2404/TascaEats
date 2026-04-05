package pt.ul.fc.css.tascaeats.domain.user;

import jakarta.persistence.Entity;
import jakarta.validation.constraints.Email;

@Entity
public class Driver extends User {

    boolean available;

     public Driver(String name, String email, String password, Address address) {
        super(name, email, password, address);
    }

    public Driver() {

    }

    public Driver(String name, String email, String password) {
    }

    public Driver(String name, @Email(message = "Email should be valid") String email, String password, String address) {
    }
}
