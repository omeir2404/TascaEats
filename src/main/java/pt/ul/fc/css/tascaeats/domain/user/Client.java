package pt.ul.fc.css.tascaeats.domain.user;


import jakarta.persistence.Entity;

@Entity
public class Client extends User {



    public Client(String name, String email, String password, Address address) {
        super(name, email, password, address);
    }

    public Client() {

    }
}
