package pt.ul.fc.css.tascaeats.domain;

import java.time.LocalDate;


public class PagamentoDinheiro extends Pagamento {

    private double valorRecebido;
    private double troco;

    public PagamentoDinheiro(long id, double valor, LocalDate dataPagamento, String referenciaPagamento, double valorRecebido) {
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
