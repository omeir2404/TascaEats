package pt.ul.fc.css.tascaeats.repository.utilizadorRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.ul.fc.css.tascaeats.domain.utilizador.RestauranteAdmin;

public interface IRestauranteAdminRepository extends JpaRepository<RestauranteAdmin, Long> {
     RestauranteAdmin findByEmail(String email);
}
