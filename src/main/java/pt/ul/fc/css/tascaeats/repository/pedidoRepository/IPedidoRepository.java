package pt.ul.fc.css.tascaeats.repository.pedidoRepository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import pt.ul.fc.css.tascaeats.domain.EstadoPedido;
import pt.ul.fc.css.tascaeats.domain.Pedido;

public interface IPedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByClienteeId(Long clienteId);
    List<Pedido> findByRestauranteId(Long restauranteId);
    List<Pedido> findByEstado(EstadoPedido estado);
    List<Pedido> findByClienteeIdAndEstado(Long clienteId, EstadoPedido estado);
    boolean existsByIdAndEstado(Long id, EstadoPedido estado);
}
