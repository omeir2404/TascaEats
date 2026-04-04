package pt.ul.fc.css.tascaeats.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import jakarta.persistence.ManyToOne;

@Entity
public class Restaurante{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String nome;

    @NotBlank
    @Column(nullable = false)
    private String nif;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoRestaurante estado = EstadoRestaurante.FECHADO;

    @OneToOne
    @JoinColumn(name = "morada_id", nullable = false, unique = true)
    private Morada morada; 

    @OneToOne
    @JoinColumn(name = "catalogo_produtos_id", nullable = false, unique = true)
    private CatalogoProdutos catalogoProdutos;

    @OneToOne
    @JoinColumn(name = "catalogo_entregas_id", nullable = false)
    private CatalogoEntregas catalogoEntregas;

    @ManyToOne
    @JoinColumn(name = "administrador_id", nullable = false)
    private Administrador administrador;

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

    public void setEstado(EstadoRestaurante estado){
        this.estado = estado;
    }

    public boolean isAberto() {
    return estado == EstadoRestaurante.ABERTO;
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
    public Administrador getAdministrador(){
        return administrador;
    }

    public void setAdministrador(Administrador administrador){
        this.administrador = administrador;
    }

}