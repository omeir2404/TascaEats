package pt.ul.fc.css.tascaeats.dto.pedidoDTO;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;


public class CriarPedidoDTO {

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

    public Long getClienteeId() {
        return clienteId;
    }

    public void setClienteeId(Long clienteId) {
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
