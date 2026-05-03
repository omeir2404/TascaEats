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
@DiscriminatorValue("DINHEIRO")
public class PagamentoDinheiro extends Pagamento {

    @Column(name = "valor_recebido")
    private double valorRecebido;

    @Column(name = "valor_troco")
    private Double valorTroco;

    protected PagamentoDinheiro() {
    }

    public PagamentoDinheiro(Long id, double valor, LocalDate dataPagamento, String referenciaPagamento, double valorRecebido) {
        super(id, valor, dataPagamento, referenciaPagamento);
        this.valorRecebido = valorRecebido;
    }

    public PagamentoDinheiro(Long id, double valor, LocalDate dataPagamento, String referenciaPagamento, double valorRecebido, Double valorTroco) {
        super(id, valor, dataPagamento, referenciaPagamento);
        this.valorRecebido = valorRecebido;
        this.valorTroco = valorTroco;
    }

    // ################### MÉTODOS DE NEGÓCIO ######################################################################

    @Override
    public void validarDetalhes() {
        if (valorRecebido < getValor()) {
            throw new IllegalArgumentException("Valor recebido insuficiente para concluir o pagamento em dinheiro.");
        }
        this.valorTroco = valorRecebido - getValor();
    }

    @Override
    public String obterInstrucoesPagamento() {
        return "Pagar " + getValor() + "€ em dinheiro. Troco previsto: " + valorTroco + "€.";
    }

// ###################### GETTERS e SETTERS #####################################################################


    public double getValorRecebido() {
        return valorRecebido;
    }

    public void setValorRecebido(double valorRecebido) {
        this.valorRecebido = valorRecebido;
    }


    public Double getValorTroco() {return valorTroco;}

    public void setValorTroco(Double valorTroco) {this.valorTroco = valorTroco;}

    public Double getTroco() {return valorTroco;}

}
