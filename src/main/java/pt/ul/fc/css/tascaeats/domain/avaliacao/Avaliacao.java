package pt.ul.fc.css.tascaeats.domain.avaliacao;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import pt.ul.fc.css.tascaeats.domain.pedido.Pedido;
import pt.ul.fc.css.tascaeats.domain.restaurante.Restaurante;
import pt.ul.fc.css.tascaeats.domain.utilizador.Cliente;

@Entity
@Table(name = "avaliacao")
public class Avaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private int classificacao;

    private String comentario;

    //Cliente que faz a avaliação
    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    //Restaurante avaliado
    @ManyToOne
    @JoinColumn(name = "restaurante_id", nullable= false)
    private Restaurante restaurante;

    @OneToOne
    @JoinColumn(name = "pedido_id", nullable = false, unique = true)
    private Pedido pedido;


    // *********CONSTRUTORES**********
    protected Avaliacao() {
    }

    public Avaliacao(int classificacao, String comentario, Cliente cliente, Restaurante restaurante, Pedido pedido){
        this.classificacao = classificacao;
        this.comentario = comentario;
        this.cliente = cliente;
        this.restaurante = restaurante;
        this.pedido = pedido;
    }

}
