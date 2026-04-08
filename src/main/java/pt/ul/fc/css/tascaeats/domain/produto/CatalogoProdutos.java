package pt.ul.fc.css.tascaeats.domain.produto;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import pt.ul.fc.css.tascaeats.domain.restaurante.Restaurante;

@Entity
public class CatalogoProdutos {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "catalogoProdutos")
    private Restaurante restaurante;

    @OneToMany(mappedBy = "catalogoProdutos", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Produto> produtos = new ArrayList<>();
    

    // *********CONSTRUTORES**********

    public CatalogoProdutos(){
    }

    public CatalogoProdutos(Restaurante restaurante) {
        this();
        if (restaurante == null) {
            throw new IllegalArgumentException("Restaurante é obrigatório");
        }
        this.restaurante = restaurante;
        restaurante.setCatalogoProdutos(this);
    }


    // *********GETTERS E SETTERS**********

    //Id
    public Long getId(){
        return id;
    }

    //Restaurante
	public Restaurante getRestaurante() {
		return restaurante;
	}

    public void setRestaurante(Restaurante restaurante) {
        if (this.restaurante != null && this.restaurante != restaurante) {
            this.restaurante.setCatalogoProdutos(null);
        }
        this.restaurante = restaurante;
        if (restaurante != null && restaurante.getCatalogoProdutos() != this) {
            restaurante.setCatalogoProdutos(this);
        }
    }

    //Produtos
    public List<Produto> getProdutos() {
        return produtos;
    }

    public void setProdutos(List<Produto> produtos){
        for(Produto p : this.produtos){
            p.setCatalogoProdutos(null);
        }
        this.produtos.clear();
        if(produtos != null){
            for(Produto produto : produtos){
                addProduto(produto);
            }
        }
    }

    public void addProduto(Produto produto){
        if(produto == null){
            return;
        }
        if(produto.isApagado()){
            throw new IllegalStateException("Não é possível adicionar ao catálogo um produto apagado!");
        }
        if(!produtos.contains(produto)){
            produtos.add(produto);
            produto.setCatalogoProdutos(this);
        }
    }

    public void removeProduto(Produto produto){
        if(produto == null){
            return;
        }
        produtos.remove(produto);
        produto.setCatalogoProdutos(null);
    }

}
