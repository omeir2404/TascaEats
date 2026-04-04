package pt.ul.fc.css.tascaeats.domain;

import java.time.LocalDate;


public class PagamentoMultibanco extends Pagamento {

    private String entidade;
    private String referencia;

    public PagamentoMultibanco(long id, double valor, LocalDate dataPagamento, String referenciaPagamento, String entidade, String referencia) {
        super(id, valor, dataPagamento, referenciaPagamento);
        this.entidade = entidade;
        this.referencia = referencia;
    }

    @Override
    public void validarDetalhes() {
        if (entidade == null || !entidade.matches("\\d{5}")) {
            throw new IllegalArgumentException("Entidade inválida. Deve conter 5 dígitos.");
        }
        if (referencia == null || !referencia.matches("\\d{9}")) {
            throw new IllegalArgumentException("Referência inválida. Deve conter 9 dígitos.");
        }
    }

    @Override
    public String obterInstrucoesPagamento() {
        return "Pague " + getValor() + "€ por Multibanco com entidade " + entidade + " e referência " + referencia + ".";
    }

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
