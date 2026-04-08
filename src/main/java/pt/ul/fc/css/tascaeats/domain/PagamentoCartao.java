package pt.ul.fc.css.tascaeats.domain;


public class PagamentoCartao extends Pagamento {

    private String numeroCartao;

    public String getNumeroCartao() {
        return numeroCartao;
    }

    public void setNumeroCartao(String numeroCartao) {
        this.numeroCartao = numeroCartao;
    }
}
