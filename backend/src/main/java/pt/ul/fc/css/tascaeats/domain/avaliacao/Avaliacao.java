// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################

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


    // *********GETTERS E SETTERS**********

    public long getId(){
        return id;
    }

    public int getClassificacao(){
        return classificacao;
    }

    public void setClassificacao(int classificacao){
        this.classificacao = classificacao;
    }

    public String getComentario(){
        return comentario;
    }

    public void setComentario(String comentario){
        this.comentario = comentario;
    }

    public Cliente getCliente(){
        return cliente;
    }

    public void setCliente(Cliente cliente){
        this.cliente = cliente;
    }

    public Restaurante getRestaurante() {
        return restaurante;
    }

    public void setRestaurante(Restaurante restaurante) {
        this.restaurante = restaurante;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

}
