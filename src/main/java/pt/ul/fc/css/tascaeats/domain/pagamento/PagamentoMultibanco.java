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

    @Column(name = "entidade")
    private String entidade;
    @Column(name = "referencia")
    private String referencia;

    protected PagamentoMultibanco() {
    }

    public PagamentoMultibanco(Long id, double valor, LocalDate dataPagamento, String referenciaPagamento, String entidade, String referencia) {
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

// ###################### GETTERS e SETTERS #####################################################################


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
