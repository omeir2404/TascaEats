package pt.ul.fc.css.tascaeats.repository.utilizadorRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.ul.fc.css.tascaeats.domain.Utilizador.Entregador;

public interface IEntregadorRepository extends JpaRepository<Entregador, Long> {
     Entregador findByEmail(String email);
     Entregador findByAvailable(boolean available);
}
