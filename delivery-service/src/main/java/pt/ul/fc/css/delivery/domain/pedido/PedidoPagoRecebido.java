// ##############################################################
// 					  TASCAEATS						#
// 					CSS GRUPO: 25						#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.delivery.domain.pedido;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "pedidos_pagos_recebidos")
public class PedidoPagoRecebido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pedido_id", nullable = false, unique = true)
    private Long pedidoId;

    @Column(name = "cliente_id")
    private Long clienteId;

    @Column(name = "endereco_entrega", nullable = false)
    private String enderecoEntrega;

    @Column(name = "recebido_em", nullable = false)
    private LocalDateTime recebidoEm;

    protected PedidoPagoRecebido() {
    }

    public PedidoPagoRecebido(Long pedidoId, Long clienteId, String enderecoEntrega) {
        this.pedidoId = pedidoId;
        this.clienteId = clienteId;
        this.enderecoEntrega = enderecoEntrega;
        this.recebidoEm = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public String getEnderecoEntrega() {
        return enderecoEntrega;
    }

    public LocalDateTime getRecebidoEm() {
        return recebidoEm;
    }

    public void atualizarRecebimento(String enderecoEntrega) {
        if (enderecoEntrega != null && !enderecoEntrega.isBlank()) {
            this.enderecoEntrega = enderecoEntrega;
        }
        this.recebidoEm = LocalDateTime.now();
    }
}
