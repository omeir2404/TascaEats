package pt.ul.fc.css.tascaeats.repository.pagamentoRepository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import pt.ul.fc.css.tascaeats.domain.pagamento.Pagamento;

public interface IPagamentoRepository extends JpaRepository<Pagamento, Long> {
    Pagamento findByReferenciaPagamento(String referenciaPagamento);
    List<Pagamento> findByDataPagamento(LocalDate dataPagamento);
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                