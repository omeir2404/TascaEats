package pt.ul.fc.css.tascaeats.domain.user;


import jakarta.persistence.Entity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Entity
public class Client extends User {

    @Getter
    @Setter
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "address_id")
    private Address address;

    public Client(String name, String email, String password, Address address) {
        super(name, email, password);
        this.address = address;
    }
    public Client() {}

    public Client(String name, @Email(message = "Email should be valid") String email, String password, String address) {
    }
}
