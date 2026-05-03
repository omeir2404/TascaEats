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
@DiscriminatorValue("CARTAO")
public class PagamentoCartao extends Pagamento {

    @Column(name = "numero_cartao", nullable = true)
    private String numeroCartao;

    protected PagamentoCartao() {
    }

    public PagamentoCartao(Long id, double valor, LocalDate dataPagamento, String referenciaPagamento, String numeroCartao) {
        super(id, valor, dataPagamento, referenciaPagamento);
        this.numeroCartao = numeroCartao;
    }

    @Override
    public void validarDetalhes() {
        if (numeroCartao == null || !numeroCartao.matches("\\d{16}")) {
            throw new IllegalArgumentException("Número de cartão inválido. Deve conter 16 dígitos.");
        }
    }

    @Override
    public String obterInstrucoesPagamento() {
        return "Pagamento por cartão registado com referência " + getReferenciaPagamento() + ".";
    }

    public String getNumeroCartao() {
        return numeroCartao;
    }

    public void setNumeroCartao(String numeroCartao) {
        this.numeroCartao = numeroCartao;
    }
}
