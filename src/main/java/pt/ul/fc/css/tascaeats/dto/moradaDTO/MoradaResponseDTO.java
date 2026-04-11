// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.dto.moradaDTO;

public class MoradaResponseDTO {
    private String rua;
    private Integer numero;
    private String complemento;
    private String pais;
    private String distrito;
    private String cidade;
    private String codigoPostal;

    public MoradaResponseDTO() {
    }

    public MoradaResponseDTO(String rua, Integer number, String complemento, String pais, String distrito, String cidade, String codigoPostal) {
        this.rua = rua;
        this.numero = number;
        this.complemento = complemento;
        this.pais = pais;
        this.distrito = distrito;
        this.cidade = cidade;
        this.codigoPostal = codigoPostal;
    }


    public String getRua() {
        return rua;
    }

    public void setRua(String rua) {
        this.rua = rua;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getDistrito() {
        return distrito;
    }

    public void setDistrito(String distrito) {
        this.distrito = distrito;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(String codigoPostal) {
        this.codigoPostal = codigoPostal;
    }
}

