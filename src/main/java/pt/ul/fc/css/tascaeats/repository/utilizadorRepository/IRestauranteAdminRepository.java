package pt.ul.fc.css.tascaeats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.ul.fc.css.tascaeats.domain.Utilizador.RestauranteAdmin;

public interface IRestauranteAdminRepository extends JpaRepository<RestauranteAdmin, Long> {
     RestauranteAdmin findByEmail(String email);
}
