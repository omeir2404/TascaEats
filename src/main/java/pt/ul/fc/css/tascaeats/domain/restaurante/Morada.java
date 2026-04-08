package pt.ul.fc.css.tascaeats.domain.restaurante;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Morada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String rua;

    @Column(nullable = false)
    private Integer numeroPorta;

    @Column(nullable = false)
    private String codigoPostal;

    @Column(nullable = false)
    private String localidade;

    protected Morada() {
    }

    public Morada(String rua, Integer numeroPorta, String codigoPostal, String localidade) {
        this.rua = rua;
        this.numeroPorta = numeroPorta;
        this.codigoPostal = codigoPostal;
        this.localidade = localidade;
    }

    public Long getId() {
        return id;
    }

    public String getRua() {
        return rua;
    }

    public void setRua(String rua) {
        this.rua = rua;
    }

    public Integer getNumeroPorta() {
        return numeroPorta;
    }

    public void setNumeroPorta(Integer numeroPorta) {
        this.numeroPorta = numeroPorta;
    }

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(String codigoPostal) {
        this.codigoPostal = codigoPostal;
    }

    public String getLocalidade() {
        return localidade;
    }

    public void setLocalidade(String localidade) {
        this.localidade = localidade;
    }
}
