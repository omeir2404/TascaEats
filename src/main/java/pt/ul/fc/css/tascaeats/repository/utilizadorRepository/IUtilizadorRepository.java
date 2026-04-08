package pt.ul.fc.css.tascaeats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.ul.fc.css.tascaeats.domain.Utilizador.Utilizador;

import java.util.Optional;

@Repository
public interface IUtilizadorRepository extends JpaRepository<Utilizador, Long> {
        Optional<Utilizador> findByEmail(String email);
}