package pt.ul.fc.css.tascaeats.domain.user;


import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Entity
public class Client extends User {

    @Getter
    @Setter
    @OneToOne
    private Address address;

    public Client(String name, String email, String password, Address address) {
        super(name, email, password);
        address.setUser(this);
    }
    public Client() {}

    public Client(String name, @Email(message = "Email should be valid") String email, String password, String address) {
    }
}
