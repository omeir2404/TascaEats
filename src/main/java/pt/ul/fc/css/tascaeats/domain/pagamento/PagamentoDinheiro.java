package pt.ul.fc.css.tascaeats.domain.pagamento;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("DINHEIRO")
public class PagamentoDinheiro extends Pagamento {

    @Column(name = "valor_recebido")
    private double valorRecebido;
    @Column(name = "troco")
    private double troco;

    protected PagamentoDinheiro() {
    }

    public PagamentoDinheiro(Long id, double valor, LocalDate dataPagamento, String referenciaPagamento, double valorRecebido) {
        super(id, valor, dataPagamento, referenciaPagamento);
        this.valorRecebido = valorRecebido;
    }

    @Override
    public void validarDetalhes() {
        if (valorRecebido < getValor()) {
            throw new IllegalArgumentException("Valor recebido insuficiente para concluir o pagamento em dinheiro.");
        }
        this.troco = valorRecebido - getValor();
    }

    @Override
    public String obterInstrucoesPagamento() {
        return "Pagar " + getValor() + "€ em dinheiro. Troco previsto: " + troco + "€.";
    }

// ###################### GETTERS e SETTERS #####################################################################


    public double getValorRecebido() {
        return valorRecebido;
    }

    public void setValorRecebido(double valorRecebido) {
        this.valorRecebido = valorRecebido;
    }

    public double getTroco() {
        return troco;
    }

    public void setTroco(double troco) {
        this.troco = troco;
    }
}
