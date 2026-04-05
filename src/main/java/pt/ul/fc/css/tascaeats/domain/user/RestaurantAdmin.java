package pt.ul.fc.css.tascaeats.domain.user;

public class RestaurantAdmin extends AbstractUser{
    public RestaurantAdmin(String name, String email, String password, Address address) {
        super(name, email, password, address);
    }
}
