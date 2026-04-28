package pt.ul.fc.css.tascaeats.domain.menu;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.LastModifiedBy;
import pt.ul.fc.css.tascaeats.domain.produto.Produto;
import pt.ul.fc.css.tascaeats.domain.restaurante.Restaurante;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "menus")
public class Menu {

    @LastModifiedBy
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String nome;

    @ManyToMany(mappedBy = "menus")
    private List<Restaurante> restaurantes = new ArrayList<>();

     @ManyToMany
     @JoinTable(name = "menu_produtos", joinColumns = @JoinColumn(name = "menu_id"), inverseJoinColumns = @JoinColumn(name = "produto_id"))
     private List<Produto> produtos = new ArrayList<>();

    // *********CONSTRUTORES**********

    protected Menu() {
    }

    public Menu(String nome) {
        this.nome = nome;
    }


}
