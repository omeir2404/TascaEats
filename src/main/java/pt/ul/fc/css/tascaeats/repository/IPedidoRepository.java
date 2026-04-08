package pt.ul.fc.css.tascaeats.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import pt.ul.fc.css.tascaeats.domain.Pedido;

public interface IPedidoRepository extends JpaRepository<Pedido, Long> {
}
