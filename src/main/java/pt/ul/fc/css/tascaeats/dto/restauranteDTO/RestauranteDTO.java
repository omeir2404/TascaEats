package pt.ul.fc.css.tascaeats.dto;

import pt.ul.fc.css.tascaeats.domain.EstadoRestaurante;

public class RestauranteDTO {

	private Long id;
	private String nome;
	private String nif;
	private MoradaDTO morada;
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

    //Nif
	public String getNif() {
		return nif;
	}

	public void setNif(String nif) {
		this.nif = nif;
	}

    //Morada
	public MoradaDTO getMorada() {
		return morada;
	}

	public void setMorada(MoradaDTO morada) {
		this.morada = morada;
	}

    //Estado
	public EstadoRestaurante getEstado() {
		return estado;
	}

	public void setEstado(EstadoRestaurante estado) {
		this.estado = estado;
	}
}
