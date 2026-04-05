package pt.ul.fc.css.tascaeats.domain.user;

public class Driver extends AbstractUser {

     public Driver(String name, String email, String password, Address address) {
        super(name, email, password, address);
    }
}
