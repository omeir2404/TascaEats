package pt.ul.fc.css.tascaeats.domain.user;

import jakarta.persistence.Entity;

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
}
