// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.domain.utilizador;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Email;
import pt.ul.fc.css.tascaeats.domain.restaurante.Restaurante;

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
    private List<Restaurante> restaurantes = new ArrayList<>();

    public RestauranteAdmin(String name,@Email(message = "Email should be valid") String email, String password) {
        super(name, email, password);
    }
    public RestauranteAdmin() {

    }

}
