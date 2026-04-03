package pt.ul.fc.css.tascaeats.domain;

import java.time.LocalDate;


public abstract class Pagamento {

    private Long id;

    private double valor;
    private LocalDate dataPagamento;
    private String referenciaPagamento;


}
