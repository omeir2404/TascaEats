// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.domain.pagamento;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("MULTIBANCO")
public class PagamentoMultibanco extends Pagamento {

    // Allow nulls at the DB level because this is a single-table inheritance mapping
    // and other payment types will have NULL in these columns. Validation is enforced
    // in `validarDetalhes()` for MULTIBANCO payments.
    @Column(name = "entidade", nullable = true)
    private String entidade;

    @Column(name = "referencia", nullable = true)
    private String referencia;

    @Column(name = "bandeira_cartao", nullable = true)
    private String bandeiraCartao;

    protected PagamentoMultibanco() {
    }

    public PagamentoMultibanco(Long id,
                               double valor,
                               LocalDate dataPagamento,
                               String referenciaPagamento,
                               String entidade,
                               String referencia,
                               String bandeiraCartao) {
        super(id, valor, dataPagamento, referenciaPagamento);
        this.entidade = entidade;
        this.referencia = referencia;
        this.bandeiraCartao = bandeiraCartao;
    }

    public PagamentoMultibanco(Long id,
                               double valor,
                               LocalDate dataPagamento,
                               String referenciaPagamento,
                               String entidade,
                               String referencia) {
        this(id, valor, dataPagamento, referenciaPagamento, entidade, referencia, "MULTIBANCO");
    }

    // ################### MÉTODOS DE NEGÓCIO ###################

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
        return "Pagar " + getValor() +
                "€ via Multibanco com entidade " + entidade +
                ", referência " + referencia +
                " e bandeira " + bandeiraCartao + ".";
    }

    // ################### GETTERS / SETTERS ###################

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

    public String getBandeiraCartao() {
        return bandeiraCartao;
    }

    public void setBandeiraCartao(String bandeiraCartao) {
        this.bandeiraCartao = bandeiraCartao;
    }
}
