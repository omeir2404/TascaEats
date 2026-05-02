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
@DiscriminatorValue("MBWAY")
public class PagamentoMBWay extends Pagamento {


    @Column(name = "numero_mbway")
    private String numeroMbWay;

    protected PagamentoMBWay() {
    }

    public PagamentoMBWay(Long id, double valor, LocalDate dataPagamento, String referenciaPagamento, String numeroMbWay){
        super(id, valor, dataPagamento, referenciaPagamento);
        this.numeroMbWay = numeroMbWay;
    }


    // ################### MÉTODOS DE NEGÓCIO ######################################################################

    @Override
    public void validarDetalhes() {
        if (this.numeroMbWay == null || this.numeroMbWay.length() != 9 || this.numeroMbWay.charAt(0) != '9') {
            throw new IllegalArgumentException("Número de telemóvel inválido para MBWay.");
        }
    }

    @Override
    public String obterInstrucoesPagamento() {
        return "Por favor, confirme o pagamento de " + getValor() + "€ na app MBWay para o número " + numeroMbWay;
    }

// ###################### GETTERS e SETTERS #####################################################################

    public String getNumeroMbWay() { return numeroMbWay;}

    public void setNumeroMbWay(String numeroMbWay) {this.numeroMbWay = numeroMbWay;}

}
