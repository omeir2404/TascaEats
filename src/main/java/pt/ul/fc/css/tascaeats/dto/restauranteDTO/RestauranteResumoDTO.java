package pt.ul.fc.css.tascaeats.dto;

import pt.ul.fc.css.tascaeats.domain.EstadoRestaurante;

public class RestauranteResumoDTO {

	private Long id;
	private String nome;
	private EstadoRestaurante estado;


	// *********GETTERS E SETTERS**********

	//Id
	public Long getId() {
		return id;
	}

	//Nome
	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	//Estado
	public EstadoRestaurante getEstado() {
		return estado;
	}

	public void setEstado(EstadoRestaurante estado) {
		this.estado = estado;
	}
}
