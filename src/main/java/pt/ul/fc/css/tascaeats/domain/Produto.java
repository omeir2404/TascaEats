import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.ArrayList;

@Entity
public class Produto{
    
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String nome;

    @Positive
    @NotNull
    @Column(nullable = false)
    private Double preco;

    @Column(nullable = false)
    private Boolean disponivel= true;

    @NotBlank
    @Column(nullable = false)
    private String descricao;

    @NotNull
    @Column(nullable = false)
    private Boolean apagado = false;

    @ManyToOne
    @JoinColumn(name= "catalogo_produtos_id", nullable= false)
    private CatalogoProdutos catalogoProdutos;

    @ManyToMany
    @JoinTable(name = "pedido_produto", joinColumns = @JoinColumn(name= "produto_id"), inverseJoinColumns= @JoinColumn(name = "pedido_id"))
    private List<Pedido> pedidos = new ArrayList<>();
    
    

}