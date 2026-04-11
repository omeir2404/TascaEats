package pt.ul.fc.css.tascaeats.dto.moradaDTO;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class MoradaDTO {

	@NotBlank(message = "A 'rua' da morada é obrigatória.")
	private String rua;
	@NotNull(message = "O 'numero' da morada é obrigatório.")
	@Positive(message = "O 'numero' da morada deve ser positivo.")
	private Integer numero;
	@Nullable
	private String complemento;

	@NotBlank(message = "O 'codigoPostal' da morada é obrigatório.")
	private String codigoPostal;

	@NotBlank(message = "A 'localidade' da morada é obrigatória.")
	private String cidade;
	@NotBlank(message = "O 'distrito' da morada é obrigatória.")
	private String distrito;
	@NotBlank(message = "O 'pais' da morada é obrigatória.")
	private String pais;


    // *********GETTERS E SETTERS**********

    //Rua
	public String getRua() {
		return rua;
	}

	public void setRua(String rua) {
		this.rua = rua;
	}

    //Número da Porta
	public Integer getNumero() {
		return numero;
	}

	public void setNumero(Integer numero) {
		this.numero = numero;
	}


    //Código Postal
	public String getCodigoPostal() {
		return codigoPostal;
	}

	public void setCodigoPostal(String codigoPostal) {
		this.codigoPostal = codigoPostal;
	}

	//Complemento
	public String getComplemento() {
		return complemento;
	}

	public void setComplemento(String complemento) {
		this.complemento = complemento;
	}

    //Localidade
	public String getCidade() {
		return cidade;
	}

	public void setCidade(String cidade) {
		this.cidade = cidade;
	}

	//Distrito
	public String getDistrito() {
		return distrito;
	}

	public void setDistrito(String distrito) {
		this.distrito = distrito;
	}

	//País
	public String getPais() {
		return pais;
	}

	public void setPais(String pais) {
		this.pais = pais;
	}
}
