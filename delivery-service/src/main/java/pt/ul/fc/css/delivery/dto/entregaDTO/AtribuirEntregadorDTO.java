package pt.ul.fc.css.delivery.dto.entregaDTO;

public class AtribuirEntregadorDTO {

    private Long pedidoId;

    //********MÉTODOS*********

    protected AtribuirEntregadorDTO() {
    }


    //********GETTERS E SETTERS*********

    public AtribuirEntregadorDTO(Long pedidoId) {
        this.pedidoId = pedidoId;
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
    }
}
