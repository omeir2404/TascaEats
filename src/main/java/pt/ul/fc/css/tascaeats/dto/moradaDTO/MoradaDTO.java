package pt.ul.fc.css.tascaeats.dto.moradaDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class MoradaDTO {

	@NotBlank(message = "A 'rua' da morada é obrigatória.")
	private String rua;

	@NotNull(message = "O 'numeroPorta' da morada é obrigatório.")
	@Positive(message = "O 'numeroPorta' da morada deve ser positivo.")
	private Integer numeroPorta;

	@NotBlank(message = "O 'codigoPostal' da morada é obrigatório.")
	private String codigoPostal;

	@NotBlank(message = "A 'localidade' da morada é obrigatória.")
	private String localidade;


    // *********GETTERS E SETTERS**********

    //Rua
	public String getRua() {
		return rua;
	}

	public void setRua(String rua) {
		this.rua = rua;
	}

    //Número da Porta
	public Integer getNumeroPorta() {
		return numeroPorta;
	}

	public void setNumeroPorta(Integer numeroPorta) {
		this.numeroPorta = numeroPorta;
	}


    //Código Postal
	public String getCodigoPostal() {
		return codigoPostal;
	}

	public void setCodigoPostal(String codigoPostal) {
		this.codigoPostal = codigoPostal;
	}

    //Localidade
	public String getLocalidade() {
		return localidade;
	}

	public void setLocalidade(String localidade) {
		this.localidade = localidade;
	}
}
