package pt.ul.fc.css.tascaeats.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import pt.ul.fc.css.tascaeats.domain.Entrega;

public interface IEntregaRepository extends JpaRepository<Entrega, Long> {
}
