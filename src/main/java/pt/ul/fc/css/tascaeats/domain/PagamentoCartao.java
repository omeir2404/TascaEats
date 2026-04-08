package pt.ul.fc.css.tascaeats.domain;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("CARTAO")
public class PagamentoCartao extends Pagamento {

    @Column(name = "numero_cartao")
    private String numeroCartao;

    protected PagamentoCartao() {
    }

    public PagamentoCartao(Long id, double valor, LocalDate dataPagamento, String referenciaPagamento, String numeroCartao){
        super(id, valor, dataPagamento, referenciaPagamento);
        this.numeroCartao = numeroCartao;
    }

    @Override
    public void validarDetalhes() {
        if (this.numeroCartao == null || !this.numeroCartao.matches("\\d{16}")) {
            throw new IllegalArgumentException("Número de cartão inválido. Deve conter 16 dígitos.");
        }
    }

    @Override
    public String obterInstrucoesPagamento() {
        String ultimos4Digitos = numeroCartao.substring(numeroCartao.length() - 4);
        return "O pagamento de " + getValor() + "€ será processado no cartão terminado em " + ultimos4Digitos + ".";
    }

// ###################### GETTERS e SETTERS #####################################################################

    public String getNumeroCartao() {
        return numeroCartao;
    }

    public void setNumeroCartao(String numeroCartao) {
        this.numeroCartao = numeroCartao;
    }
}
