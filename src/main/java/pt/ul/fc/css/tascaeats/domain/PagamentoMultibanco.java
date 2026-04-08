package pt.ul.fc.css.tascaeats.domain;


public class PagamentoMultibanco extends Pagamento {

    private String entidade;
    private String referencia;

    public String getEntidade() {
        return entidade;
    }

    public void setEntidade(String entidade) {
        this.entidade = entidade;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }
}
