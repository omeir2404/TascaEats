package pt.ul.fc.css.tascaeats.domain.user;


import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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

}
