package pt.ul.fc.css.tascaeats.domain;

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

    @NotNull
    @Column(nullable = false)
    private boolean disponivel = true;

    @NotBlank
    @Column(nullable = false)
    private String descricao;

    @NotNull
    @Column(nullable = false)
    private boolean apagado = false;

    @ManyToOne
    @JoinColumn(name= "catalogo_produtos_id", nullable= false)
    private CatalogoProdutos catalogoProdutos;

    @ManyToMany
    @JoinTable(name = "pedido_produto", joinColumns = @JoinColumn(name= "produto_id"), inverseJoinColumns= @JoinColumn(name = "pedido_id"))
    private List<Pedido> pedidos = new ArrayList<>();
    

    // *********CONSTRUTORES**********

    protected Produto(){
    }

    public Produto(String nome, Double preco, String descricao, CatalogoProdutos catalogoProdutos){
        this.nome = nome;
        this.preco = preco;
        this.disponivel = true;
        this.descricao = descricao;
        this.apagado = false;
        this.catalogoProdutos = catalogoProdutos;
    }


    // *********GETTERS E SETTERS**********

    //Id
    public Long getId(){
        return id;
    }

    public void setId(Long id){
        this.id = id;
    }

    //Nome
    public String getNome(){
        return nome;
    }

    public void setNome(String nome){
        this.nome = nome;
    }

    //Preço
    public Double getPreco(){
        return preco;
    }

    public void setPreco(Double preco){
        this.preco = preco;
    }

    //Disponível
    public boolean isDisponivel(){
        return disponivel;
    }

    public void disponibilizar() {
        if (this.apagado) {
            throw new IllegalStateException("Não é possível disponibilizar um produto apagado!");
        }
        if (this.disponivel) {
            throw new IllegalStateException("O produto já está disponível!");
        }
        this.disponivel = true;
    }

    public void indisponibilizar() {
        if (this.apagado) {
            throw new IllegalStateException("Não é possível alterar disponibilidade de um produto apagado!");
        }
        if (!this.disponivel) {
            throw new IllegalStateException("O produto já está indisponível!");
        }
        this.disponivel = false;
    }

    //Descrição
    public String getDescricao(){
        return descricao;
    }

    public void setDescricao(String descricao){
        this.descricao = descricao;
    }

    //Apagado
    public boolean isApagado(){
        return apagado;
    }

    public void apagar() {
        if (this.apagado) {
            throw new IllegalStateException("O produto já está apagado!");
        }
        this.apagado = true;
        this.disponivel = false;
    }

    //Ativar
    public void ativar() {
        if (!this.apagado) {
            throw new IllegalStateException("O produto já está ativo!");
        }
        this.apagado = false;
    }

    //Disponibilidade
    public void validarDisponibilidadeParaPedido() {
        if (this.apagado) {
            throw new IllegalStateException("O produto foi apagado e não pode ser pedido!");
        }
        if (!this.disponivel) {
            throw new IllegalStateException("O produto está indisponível para ser pedido!");
        }
    }

    //Catálogo e Setters
    public CatalogoProdutos getCatalogoProdutos() {
        return catalogoProdutos;
    }

    public void setCatalogoProdutos(CatalogoProdutos catalogoProdutos) {
        this.catalogoProdutos = catalogoProdutos;
    }

    public List<Pedido> getPedidos() {
        return pedidos;
    }

    public void setPedidos(List<Pedido> pedidos) {
        this.pedidos = pedidos;
    }
}