package pt.ul.fc.css.tascaeats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.ul.fc.css.tascaeats.domain.user.RestaurantAdmin;

public interface RestaurantAdminRepository extends JpaRepository<RestaurantAdmin, Long> {
     RestaurantAdmin findByEmail(String email);
}
