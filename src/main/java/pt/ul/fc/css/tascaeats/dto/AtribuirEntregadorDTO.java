package pt.ul.fc.css.tascaeats.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class AtribuirEntregadorDTO {

    @NotNull(message="Pedido Id não pode ser nulo")
    @Positive(message = "Pedido Id deve ser maior que 0")
    private Long pedidoId;

    @NotNull(message="Entregador Id não pode ser nulo")
    @Positive(message = "Entregador Id deve ser maior que 0")
    private Long entregadorId;


    protected AtribuirEntregadorDTO() {}

    public AtribuirEntregadorDTO(Long pedidoId, Long entregadorId){
        this.pedidoId = pedidoId;
        this.entregadorId = entregadorId;
    }


// ###################### GETTERS e SETTERS #####################################################################

    public Long getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
    }

    public Long getEntregadorId() {
        return entregadorId;
    }

    public void setEntregadorId(Long entregadorId) {
        this.entregadorId = entregadorId;
    }

    @Override
    public String toString(){
        return "AtribuirEntregadorDTO{" + "pedidoId=" + pedidoId +
                    ", entregadorId=" + entregadorId +"}";
    }
}
