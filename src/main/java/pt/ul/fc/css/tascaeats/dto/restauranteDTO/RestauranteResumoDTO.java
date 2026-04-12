// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.dto.restauranteDTO;

import pt.ul.fc.css.tascaeats.domain.restaurante.EstadoRestaurante;

public class RestauranteResumoDTO {

	private Long id;
	private String nome;
	private EstadoRestaurante estado;


	// ###################### GETTERS e SETTERS #####################################################################

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
