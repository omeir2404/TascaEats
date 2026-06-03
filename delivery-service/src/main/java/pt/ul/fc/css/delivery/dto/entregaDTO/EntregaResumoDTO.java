// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################

package pt.ul.fc.css.delivery.dto.entregaDTO;

import java.time.LocalDateTime;

import pt.ul.fc.css.delivery.domain.entrega.EstadoEntrega;

public class EntregaResumoDTO {

    private Long id;
    private Long pedidoId;
    private Long entregadorId;
    private String entregadorNome;
    private String entregadorEmail;
    private EstadoEntrega estado;
    private LocalDateTime horaRecolha;
    private LocalDateTime horaEntrega;
    private String enderecoEntrega;

    public EntregaResumoDTO() {
    }

    public EntregaResumoDTO(
            Long id,
            Long pedidoId,
            Long entregadorId,
            String entregadorNome,
            String entregadorEmail,
            EstadoEntrega estado,
            LocalDateTime horaRecolha,
            LocalDateTime horaEntrega,
            String enderecoEntrega) {
        this.id = id;
        this.pedidoId = pedidoId;
        this.entregadorId = entregadorId;
        this.entregadorNome = entregadorNome;
        this.entregadorEmail = entregadorEmail;
        this.estado = estado;
        this.horaRecolha = horaRecolha;
        this.horaEntrega = horaEntrega;
        this.enderecoEntrega = enderecoEntrega;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getEntregadorNome() {
        return entregadorNome;
    }

    public void setEntregadorNome(String entregadorNome) {
        this.entregadorNome = entregadorNome;
    }

    public String getEntregadorEmail() {
        return entregadorEmail;
    }

    public void setEntregadorEmail(String entregadorEmail) {
        this.entregadorEmail = entregadorEmail;
    }

    public EstadoEntrega getEstado() {
        return estado;
    }

    public void setEstado(EstadoEntrega estado) {
        this.estado = estado;
    }

    public LocalDateTime getHoraRecolha() {
        return horaRecolha;
    }

    public void setHoraRecolha(LocalDateTime horaRecolha) {
        this.horaRecolha = horaRecolha;
    }

    public LocalDateTime getHoraEntrega() {
        return horaEntrega;
    }

    public void setHoraEntrega(LocalDateTime horaEntrega) {
        this.horaEntrega = horaEntrega;
    }

    public String getEnderecoEntrega() {
        return enderecoEntrega;
    }

    public void setEnderecoEntrega(String enderecoEntrega) {
        this.enderecoEntrega = enderecoEntrega;
    }
}
