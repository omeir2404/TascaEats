package pt.ul.fc.css.tascaeats.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import pt.ul.fc.css.tascaeats.domain.Entrega;

public interface IEntregaRepository extends JpaRepository<Entrega, Long> {
    List<Entrega> findByEntregadorId(Long entregadorId);
    Optional<Entrega> findByPedidoId(Long pedidoId);
    List<Entrega> findByHoraEntregaIsNull();
    boolean existsByEntregadorIdAndHoraEntregaIsNull(Long entregadorId);
}
