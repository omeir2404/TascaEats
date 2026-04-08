package pt.ul.fc.css.tascaeats.domain.restaurante;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.CascadeType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import jakarta.persistence.ManyToOne;
import pt.ul.fc.css.tascaeats.domain.produto.CatalogoProdutos;
import pt.ul.fc.css.tascaeats.domain.utilizador.RestauranteAdmin;

@Entity
public class Restaurante{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String nome;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String nif;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoRestaurante estado = EstadoRestaurante.FECHADO;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "morada_id", nullable = false, unique = true)
    private Morada morada; 

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "catalogo_produtos_id", nullable = false, unique = true)
    private CatalogoProdutos catalogoProdutos;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "catalogo_entregas_id", nullable = false)
    private CatalogoEntregas catalogoEntregas;

    @ManyToOne
    @JoinColumn(name = "administrador_id", nullable = false)
    private RestauranteAdmin administrador;


    // *********CONSTRUTORES**********

    protected Restaurante(){
    }

    public Restaurante(String nome, String nif, Morada morada, RestauranteAdmin administrador,
                   CatalogoProdutos catalogoProdutos, CatalogoEntregas catalogoEntregas){
        this.nome = nome;
        this.nif = nif;
        this.estado = EstadoRestaurante.FECHADO;
        this.morada = morada;
        this.administrador = administrador;
        this.catalogoProdutos = catalogoProdutos;
        this.catalogoEntregas = catalogoEntregas;
    }
    

    // *********GETTERS E SETTERS**********

    //Id
    public Long getId(){
        return id;
    }

    //Nome
    public String getNome(){
        return nome;
    }

    public void setNome(String nome){
        this.nome = nome;
    }

    //Nif
    public String getNif(){
        return nif;
    }

    public void setNif(String nif){
        this.nif = nif;
    }

    //Estado
    public EstadoRestaurante getEstado(){
        return estado;
    }

    public boolean isAberto() {
        return estado == EstadoRestaurante.ABERTO;
    }

    public void abrir() {
        if(this.estado == EstadoRestaurante.ABERTO){
            throw new IllegalStateException("O restaurante já está aberto!");
        }
        this.estado = EstadoRestaurante.ABERTO;
    }

    public void fechar() {
        if(this.estado == EstadoRestaurante.FECHADO){
            throw new IllegalStateException("O restaurante já está fechado!");
        }
        this.estado = EstadoRestaurante.FECHADO;
    }

    public void reservar() {
        if(this.estado == EstadoRestaurante.RESERVADO){
            throw new IllegalStateException("O restaurante já está reservado!");
        }
        this.estado = EstadoRestaurante.RESERVADO;
    }

    public void validarDisponibilidadeParaPedido() {
        if (!isAberto()) {
            throw new IllegalStateException("O restaurante está fechado. Não é possível fazer pedidos!");
        }
    }

    //Morada
    public Morada getMorada(){
        return morada;
    }

    public void setMorada(Morada morada){
        this.morada = morada;
    }

    //Catálogo de Produtos
    public CatalogoProdutos getCatalogoProdutos(){
        return catalogoProdutos;
    }

    public void setCatalogoProdutos(CatalogoProdutos catalogoProdutos){
        this.catalogoProdutos = catalogoProdutos;
    }

    //Catálogo de Entregas
    public CatalogoEntregas getCatalogoEntregas(){
        return catalogoEntregas;
    }

    public void setCatalogoEntregas(CatalogoEntregas catalogoEntregas){
        this.catalogoEntregas = catalogoEntregas;
    }

    //Administrador
    public RestauranteAdmin getAdministrador(){
        return administrador;
    }

    public void setAdministrador(RestauranteAdmin administrador){
        this.administrador = administrador;
    }

}