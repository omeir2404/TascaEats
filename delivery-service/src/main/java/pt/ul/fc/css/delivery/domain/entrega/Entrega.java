package pt.ul.fc.css.delivery.domain.entrega;

import java.time.Duration;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Entity
@Table(name = "entregas")
public class Entrega {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pedido_id", nullable = false, unique = true)
    private Long pedidoId;

    @Column(name = "entregador_id", nullable = false)
    private Long entregadorId;

    @Column(name = "endereco_entrega", nullable = false)
    private String enderecoEntrega;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoEntrega estado;

    @Column(name = "hora_recolha")
    private LocalDateTime horaRecolha;

    @Column(name = "hora_entrega")
    private LocalDateTime horaEntrega;

    protected Entrega() {
    }

    public Entrega(Long pedidoId, Long entregadorId, String enderecoEntrega) {
        this.pedidoId = pedidoId;
        this.entregadorId = entregadorId;
        this.enderecoEntrega = enderecoEntrega;
    }

    public void registarRecolha() {
        if (this.horaRecolha != null) {
            throw new IllegalStateException("A recolha deste pedido ja foi registada.");
        }
        this.horaRecolha = LocalDateTime.now();
        this.estado = EstadoEntrega.A_CAMINHO;
    }

    public void registarConclusao() {
        if (this.horaRecolha == null) {
            throw new IllegalStateException(
                    "Nao e possivel registar a entrega de um pedido que ainda nao foi recolhido.");
        }
        if (this.horaEntrega != null) {
            throw new IllegalStateException("A entrega deste pedido ja foi concluida anteriormente.");
        }
        this.horaEntrega = LocalDateTime.now();
        this.estado = EstadoEntrega.ENTREGUE;
    }

    public Duration calcularTempoDeViagem() {
        if (this.horaRecolha == null || this.horaEntrega == null) {
            throw new IllegalStateException(
                    "Nao e possivel calcular o tempo de viagem sem recolha e entrega registadas.");
        }
        return Duration.between(this.horaRecolha, this.horaEntrega);
    }