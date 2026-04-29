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
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

@Entity
@Table(name = "pagamentos")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_pagamento")
public abstract class Pagamento {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private double valor;

    @Column(name = "data_pagamento", nullable = false)
    private LocalDate dataPagamento;

    @Column(name = "referencia_pagamento")
    private String referenciaPagamento;


    // *********CONSTRUTORES**********

    protected Pagamento() {
    }

    public Pagamento(Long id, double valor, LocalDate dataPagamento, String referenciaPagamento){
        this.id = id;
        this.valor = valor;
        this.dataPagamento = dataPagamento;
        this.referenciaPagamento = referenciaPagamento;
    }


    // ################### MÉTODOS DE NEGÓCIO ######################################################################

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
