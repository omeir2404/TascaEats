package pt.ul.fc.css.tascaeats.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import pt.ul.fc.css.tascaeats.domain.EstadoPedido;
import pt.ul.fc.css.tascaeats.domain.Pedido;

public interface IPedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByClienteId(Long clienteId);
    List<Pedido> findByRestauranteId(Long restauranteId);
    List<Pedido> findByEstado(EstadoPedido estado);
    List<Pedido> findByClienteIdAndEstado(Long clienteId, EstadoPedido estado);
    boolean existsByIdAndEstado(Long id, EstadoPedido estado);
}
