package pt.ul.fc.css.tascaeats.domain;

import java.time.LocalDate;


public abstract class Pagamento {


    private Long id;

    private double valor;
    private LocalDate dataPagamento;
    private String referenciaPagamento;

    public Pagamento(long id, double valor, LocalDate dataPagamento, String referenciaPagamento){
        this.id = id;
        this.valor = valor;
        this.dataPagamento = dataPagamento;
        this.referenciaPagamento = referenciaPagamento;
    }

    public abstract void validarDetalhes();

    public abstract String obterInstrucoesPagamento();

// ###################### GETTERS e SETTERS #####################################################################
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public LocalDate getDataPagamento() {
        return dataPagamento;
    }

    public void setDataPagamento(LocalDate dataPagamento) {
        this.dataPagamento = dataPagamento;
    }

    public String getReferenciaPagamento() {
        return referenciaPagamento;
    }

    public void setReferenciaPagamento(String referenciaPagamento) {
        this.referenciaPagamento = referenciaPagamento;
    }
}
