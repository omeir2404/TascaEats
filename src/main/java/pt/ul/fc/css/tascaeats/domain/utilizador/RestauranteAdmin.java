package pt.ul.fc.css.tascaeats.domain.Utilizador;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Email;
import pt.ul.fc.css.tascaeats.domain.restaurant.Restaurant;

import java.util.ArrayList;
import java.util.List;

@Entity
public class RestauranteAdmin extends Utilizador {

    @OneToMany
    @JoinTable(
            name = "restaurant_admin_restaurants",
            joinColumns = @JoinColumn(name = "restaurant_admin_id"),
            inverseJoinColumns = @JoinColumn(name = "restaurant_id")
    )
    private List<Restaurant> restaurants = new ArrayList<>();

    public RestauranteAdmin(String name,@Email(message = "Email should be valid") String email, String password) {
        super(name, email, password);
    }
    public RestauranteAdmin() {

    }

}
