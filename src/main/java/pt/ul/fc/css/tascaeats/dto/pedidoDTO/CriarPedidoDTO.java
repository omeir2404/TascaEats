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
        return itens;
    }

    public void setItens(List<ItemPedidoDTO> itens) {
        this.itens = itens;
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
