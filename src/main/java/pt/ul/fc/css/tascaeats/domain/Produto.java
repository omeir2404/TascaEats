import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Positive;

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
    private boolean disponivel= true;

    @NotBlank
    @Column(nullable = false)
    private String descricao;

    @NotBlank
    @Column(nullable = false)
    private boolean apagado;

    @ManyToOne
    @JoinColumn(name= "restaurante_id", nullable = false)
    private Restaurante restaurante;

}