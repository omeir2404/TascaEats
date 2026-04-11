// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.dto.pedidoDTO;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;


public class CriarPedidoDTO {

    @JsonProperty("clienteId")
    @JsonAlias({"clienteeId"})
    @NotNull(message = "Clientee Id é obrigatório")
    @Positive(message = "Clientee Id deve ser maior que 0")
    private Long clienteId;

    @NotNull(message = "Restaurante Id é obrigatório")
    @Positive(message = "Restaurante Id deve ser maior que 0")
    private Long restauranteId;

    @NotEmpty(message = "A lista de produtos não pode estar vazia")
    private List<@NotNull(message = "Produto Id não pode ser nulo") @Positive(message = "Produto Id deve ser maior que 0") Long> produtosIds;

    public CriarPedidoDTO(){}

    public CriarPedidoDTO(Long clientId, Long restauranteId, List<Long> produtosIds){
        this.clienteId = clientId;
        this.restauranteId = restauranteId;
        this.produtosIds = produtosIds;
    }

    // ###################### GETTERS e SETTERS #####################################################################

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public Long getRestauranteId() {
        return restauranteId;
    }

    public void setRestauranteId(Long restauranteId) {
        this.restauranteId = restauranteId;
    }

    public List<Long> getProdutosIds() {
        return produtosIds;
    }

    public void setProdutosIds(List<Long> produtosIds) {
        this.produtosIds = produtosIds;
    }


    @Override
    public String toString(){
        return "CriarPedidoDTO{" + "clienteId=" + clienteId +
                    ", restauranteId=" + restauranteId + ", produtosIds=" + produtosIds +"}";
    }
}
