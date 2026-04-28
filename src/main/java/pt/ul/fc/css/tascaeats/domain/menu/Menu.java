package pt.ul.fc.css.tascaeats.domain.menu;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import pt.ul.fc.css.tascaeats.domain.produto.Produto;
import pt.ul.fc.css.tascaeats.domain.restaurante.Restaurante;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "menus")
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String nome;

    //@OneToMany(mappedBy = "menu")
    //private List<Restaurante> restaurantes = new ArrayList<>();

    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Produto> produtos = new ArrayList<>();


    // *********CONSTRUTORES**********

    protected Menu() {
    }

    public Menu(String nome) {
        this.nome = nome;
    }


    // *********GETTERS E SETTERS**********

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public List<Produto> getProdutos() {
        return produtos;
    }


}
