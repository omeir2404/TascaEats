package pt.ul.fc.css.tascaeats.repository.restauranteRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import pt.ul.fc.css.tascaeats.domain.restaurante.EstadoRestaurante;
import pt.ul.fc.css.tascaeats.domain.restaurante.Restaurante;

public interface IRestauranteRepository extends JpaRepository<Restaurante, Long> {

	List<Restaurante> findByNomeContainingIgnoreCase(String nome);

	List<Restaurante> findByMoradaCidadeIgnoreCase(String cidade);

	Optional<Restaurante> findByNif(String nif);

	List<Restaurante> findByEstado(EstadoRestaurante estado);
}
