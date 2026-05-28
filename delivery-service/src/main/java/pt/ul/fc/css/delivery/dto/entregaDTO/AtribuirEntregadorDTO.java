package pt.ul.fc.css.delivery.dto.entregaDTO;

public class AtribuirEntregadorDTO {

    private Long pedidoId;

    protected AtribuirEntregadorDTO() {
    }

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
