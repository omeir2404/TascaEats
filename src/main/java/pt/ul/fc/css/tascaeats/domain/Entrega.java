package pt.ul.fc.css.tascaeats.domain;

import java.time.LocalDateTime;

public class Entrega {


    private Long id;

    private LocalDateTime horaRecolha;
    private LocalDateTime horaEntrega;
    private String enderecoEntrega;
    private Long entregadorId;

    private Pedido pedido;

}
