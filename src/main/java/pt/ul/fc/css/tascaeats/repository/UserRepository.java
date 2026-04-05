package pt.ul.fc.css.tascaeats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.ul.fc.css.tascaeats.domain.user.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
        Optional<User> findByEmail(String email);
}