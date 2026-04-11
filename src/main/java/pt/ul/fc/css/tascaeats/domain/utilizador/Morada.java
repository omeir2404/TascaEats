// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.domain.utilizador;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity(name = "MoradaUtilizador")
@Table(name = "morada_utilizador")
public class Morada {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String rua;
    private Integer numero;
    private String complemento;
    private String pais;
    private String distrito;
    private String cidade;
    private String codigoPostal;

    @JsonIgnore
    @OneToOne(mappedBy = "address")
    private Cliente client;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRua() {
        return rua;
    }

    public void setRua(String street) {
        this.rua = street;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer number) {
        this.numero = number;
    }

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complement) {
        this.complemento = complement;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String country) {
        this.pais = country;
    }

    public String getDistrito() {
        return distrito;
    }

    public void setDistrito(String district) {
        this.distrito = district;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String city) {
        this.cidade = city;
    }

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(String postalCode) {
        this.codigoPostal = postalCode;
    }

    public Cliente getClient() {
        return client;
    }

    public void setClient(Cliente client) {
        this.client = client;
    }
}