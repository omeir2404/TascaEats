package pt.ul.fc.css.tascaeats.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;



public class Pedido {

    private Long id;

    private LocalDate data;
    private LocalTime hora;
    private double valorTotal;

    private EstadoPedido estado;

    private Long clienteId;
    private Long restauranteId;

    private List<Long> produtosIds = new ArrayList<>();

    private Pagamento pagamento;

}
