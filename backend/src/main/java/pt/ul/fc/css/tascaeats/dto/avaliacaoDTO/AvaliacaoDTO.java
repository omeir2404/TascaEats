// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.dto.avaliacaoDTO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class AvaliacaoDTO {

    private Long id;

    @Min(value = 1, message = "A classificação mínima é 1")
    @Max(value = 5, message = "A classificação máxima é 5")
    private int classificacao;

    private String comentario;

    @NotNull(message = "O ID do cliente é obrigatório")
    private Long clienteId;

    @NotNull(message = "O ID do restaurante é obrigatório")
    private Long restauranteId;

    @NotNull(message = "O ID do pedido é obrigatório")
    private Long pedidoId;

    public AvaliacaoDTO() {
    }

    public AvaliacaoDTO(Long id, int classificacao, String comentario, Long clienteId, Long restauranteId, Long pedidoId){
        this.id = id;
        this.classificacao = classificacao;
        this.comentario = comentario;
        this.clienteId = clienteId;
        this.restauranteId = restauranteId;
        this.pedidoId = pedidoId;
    }


    // ###################### GETTERS e SETTERS #####################################################################

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public int getClassificacao() { return classificacao; }

    public void setClassificacao(int classificacao) { this.classificacao = classificacao; }

    public String getComentario() { return comentario; }

    public void setComentario(String comentario) { this.comentario = comentario; }

    public Long getClienteId() { return clienteId; }

    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }

    public Long getRestauranteId() { return restauranteId; }

    public void setRestauranteId(Long restauranteId) { this.restauranteId = restauranteId; }

    public Long getPedidoId() { return pedidoId; }

    public void setPedidoId(Long pedidoId) { this.pedidoId = pedidoId; }

    @Override
    public String toString() {
        return "CriarAvaliacaoDTO{" + "classificacao=" + classificacao + ", comentario='" + comentario + '\'' +
                ", clienteId=" + clienteId + ", restauranteId=" + restauranteId + ", pedidoId=" + pedidoId + '}';
    }

}