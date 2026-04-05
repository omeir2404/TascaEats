package pt.ul.fc.css.tascaeats.domain.user;

public class Client extends AbstractUser{

    public Client(String name, String email, String password, Address address) {
        super(name, email, password, address);
    }
}
