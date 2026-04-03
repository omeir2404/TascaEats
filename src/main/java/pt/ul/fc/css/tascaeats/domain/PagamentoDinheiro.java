package pt.ul.fc.css.tascaeats.domain;


public class PagamentoDinheiro extends Pagamento {

    private double valorRecebido;
    private double troco;

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
