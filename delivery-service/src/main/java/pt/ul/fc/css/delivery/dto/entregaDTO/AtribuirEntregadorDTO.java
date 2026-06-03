// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################

package pt.ul.fc.css.delivery.dto.entregaDTO;

public class AtribuirEntregadorDTO {

    private Long pedidoId;
    private String enderecoEntrega;

    protected AtribuirEntregadorDTO() {
    }

    public AtribuirEntregadorDTO(Long pedidoId) {
        this.pedidoId = pedidoId;
    }

    public AtribuirEntregadorDTO(Long pedidoId, String enderecoEntrega) {
        this.pedidoId = pedidoId;
        this.enderecoEntrega = enderecoEntrega;
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
    }

    public String getEnderecoEntrega() {
        return enderecoEntrega;
    }

    public void setEnderecoEntrega(String enderecoEntrega) {
        this.enderecoEntrega = enderecoEntrega;
    }
}
