package pt.ul.fc.css.tascaeats.dto.entregaDTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class AtribuirEntregadorDTO {

    @NotNull(message="Pedido Id não pode ser nulo")
    @Positive(message = "Pedido Id deve ser maior que 0")
    private Long pedidoId;



    protected AtribuirEntregadorDTO() {}

    public AtribuirEntregadorDTO(Long pedidoId){
        this.pedidoId = pedidoId;
    }


// ###################### GETTERS e SETTERS #####################################################################

    public Long getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
    }

    @Override
    public String toString(){
        return "AtribuirEntregadorDTO{" + "pedidoId=" + pedidoId +"}";
    }
}
