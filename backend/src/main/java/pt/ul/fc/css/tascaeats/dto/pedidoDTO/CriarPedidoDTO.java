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

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import pt.ul.fc.css.tascaeats.dto.moradaDTO.MoradaDTO;


public class CriarPedidoDTO {

    @JsonProperty("clienteId")
    @NotNull(message = "Cliente Id é obrigatório")
    private Long clienteId;

    @NotEmpty(message = "A lista de itens não pode estar vazia")
    @Valid
    private List<ItemPedidoDTO> itens;

    private Long restauranteId;

    private List<Long> produtosIds;

    @Nullable
    private Long moradaId;

    @Nullable
    @Valid
    private MoradaDTO moradaNova;


    // *********CONSTRUTORES**********

    public CriarPedidoDTO(){}

    public CriarPedidoDTO(Long clienteId, List<ItemPedidoDTO> itens){
        this.clienteId = clienteId;
        this.itens = itens;
    }

    public CriarPedidoDTO(Long clienteId, Long restauranteId, List<Long> produtosIds) {
        this.clienteId = clienteId;
        this.restauranteId = restauranteId;
        setProdutosIds(produtosIds);
    }

    public CriarPedidoDTO(Long clienteId, List<ItemPedidoDTO> itens, @Nullable Long moradaId){
        this.clienteId = clienteId;
        this.itens = itens;
        this.moradaId = moradaId;
    }

    public CriarPedidoDTO(Long clienteId, List<ItemPedidoDTO> itens, @Nullable MoradaDTO moradaNova){
        this.clienteId = clienteId;
        this.itens = itens;
        this.moradaNova = moradaNova;
    }


    // ###################### GETTERS e SETTERS #####################################################################

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public List<ItemPedidoDTO> getItens() {
        if ((itens == null || itens.isEmpty()) && restauranteId != null && produtosIds != null) {
            itens = produtosIds.stream()
                    .map(produtoId -> new ItemPedidoDTO(produtoId, restauranteId, 1))
                    .toList();
        }
        return itens;
    }

    public void setItens(List<ItemPedidoDTO> itens) {
        this.itens = itens;
    }

    public Long getRestauranteId() {
        return restauranteId;
    }

    public void setRestauranteId(Long restauranteId) {
        this.restauranteId = restauranteId;
        rebuildItensFromLegacyFields();
    }

    public List<Long> getProdutosIds() {
        return produtosIds;
    }

    public void setProdutosIds(List<Long> produtosIds) {
        this.produtosIds = produtosIds;
        rebuildItensFromLegacyFields();
    }

    private void rebuildItensFromLegacyFields() {
        if (restauranteId != null && produtosIds != null) {
            this.itens = produtosIds.stream()
                    .map(produtoId -> new ItemPedidoDTO(produtoId, restauranteId, 1))
                    .toList();
        } else {
            this.itens = null;
        }
    }

    @Nullable
    public Long getMoradaId() {
        return moradaId;
    }

    public void setMoradaId(@Nullable Long moradaId) {
        this.moradaId = moradaId;
    }

    @Nullable
    public MoradaDTO getMoradaNova() {
        return moradaNova;
    }

    public void setMoradaNova(@Nullable MoradaDTO moradaNova) {
        this.moradaNova = moradaNova;
    }

    @Override
    public String toString(){
        return "CriarPedidoDTO{" + "clienteId=" + clienteId +
                    ", itens=" + itens +
                    ", moradaId=" + moradaId + ", moradaNova=" + moradaNova +"}";
    }
}
