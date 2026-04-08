package pt.ul.fc.css.tascaeats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.ul.fc.css.tascaeats.domain.user.Driver;

public interface DriverRepository extends JpaRepository<Driver, Long> {
     Driver findByEmail(String email);
     Driver findByAvailable(boolean available);
}
