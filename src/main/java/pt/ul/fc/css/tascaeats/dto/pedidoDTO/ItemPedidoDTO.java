// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.dto.pedidoDTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;


public class ItemPedidoDTO {

    @NotNull(message = "Produto Id não pode ser nulo.")
    @Positive(message = "Produto Id deve ser maior que 0.")
    private Long produtoId;

    @NotNull(message = "Restaurante Id não pode ser nulo.")
    @Positive(message = "Restaurante Id deve ser maior que 0.")
    private Long restauranteId;

    @NotNull(message = "Quantidade não pode ser nula.")
    @Positive(message = "Quantidade deve ser maior que 0.")
    private Integer quantidade;


    // *********CONSTRUTORES**********

    public ItemPedidoDTO() {
    }

    public ItemPedidoDTO(Long produtoId, Long restauranteId, Integer quantidade) {
        this.produtoId = produtoId;
        this.restauranteId = restauranteId;
        this.quantidade = quantidade;
    }


    // ###################### GETTERS e SETTERS #####################################################################

    public Long getProdutoId() {
        return produtoId;
    }

    public void setProdutoId(Long produtoId) {
        this.produtoId = produtoId;
    }

    public Long getRestauranteId() {
        return restauranteId;
    }

    public void setRestauranteId(Long restauranteId) {
        this.restauranteId = restauranteId;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }


    @Override
    public String toString() {
        return "ItemPedidoDTO{" +
                "produtoId=" + produtoId +
                ", restauranteId=" + restauranteId +
                ", quantidade=" + quantidade +
                '}';
    }
}

