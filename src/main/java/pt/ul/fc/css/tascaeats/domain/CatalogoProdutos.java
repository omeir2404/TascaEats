package pt.ul.fc.css.tascaeats.domain;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

@Entity
public class CatalogoProdutos {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "catalogoProdutos")
    private Restaurante restaurante;

    @OneToMany(mappedBy = "catalogoProdutos", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Produto> produtos = new ArrayList<>();
    
    // *********GETTERS E SETTERS**********

    //Id
    public Long getId(){
        return id;
    }

    public void setId(Long id){
        this.id = id;
    }

    //Restaurante
	public Restaurante getRestaurante() {
		return restaurante;
	}

	public void setRestaurante(Restaurante restaurante) {
		this.restaurante = restaurante;
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
