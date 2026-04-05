package pt.ul.fc.css.tascaeats.domain.user;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import pt.ul.fc.css.tascaeats.domain.restaurant.Restaurant;

import java.util.List;

@Entity
public class RestaurantAdmin extends User {

    @JoinColumn(name = "restaurant_id")
    List<Restaurant> restaurants;

    public RestaurantAdmin(String name, String email, String password, Address address) {
        super(name, email, password);
    }
    public RestaurantAdmin() {

    }
}
