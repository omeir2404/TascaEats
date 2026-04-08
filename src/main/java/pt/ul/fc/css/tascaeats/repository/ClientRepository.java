package pt.ul.fc.css.tascaeats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.ul.fc.css.tascaeats.domain.user.Client;

public interface ClientRepository extends JpaRepository<Client, Long> {
     Client findByEmail(String email);
}
