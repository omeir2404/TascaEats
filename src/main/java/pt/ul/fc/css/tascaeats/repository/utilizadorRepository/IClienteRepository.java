package pt.ul.fc.css.tascaeats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.ul.fc.css.tascaeats.domain.Utilizador.Cliente;

public interface IClienteRepository extends JpaRepository<Cliente, Long> {
     Cliente findByEmail(String email);
}
