// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.dto.produtoDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CriarProdutoDTO {

	@NotBlank(message = "O 'nome' do produto é obrigatório.")
	private String nome;

	@NotNull(message = "O 'preço' do produto é obrigatório")
	@Positive(message = "O 'preço' do produto deve ser positivo.")
	private Double preco;

	@NotBlank(message = "O 'descrição' do produto é obrigatória.")
	private String descricao;


    // *********GETTERS E SETTERS**********

    //Nome
	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

    //Preço
	public Double getPreco() {
		return preco;
	}

	public void setPreco(Double preco) {
		this.preco = preco;
	}

    //Descrição
	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}
}
