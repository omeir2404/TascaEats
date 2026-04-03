package pt.ul.fc.css.tascaeats.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import pt.ul.fc.css.tascaeats.domain.Pagamento;

public interface IPagamentoRepository extends JpaRepository<Pagamento, Long> {
}
