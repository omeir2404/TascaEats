// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.domain.pedido;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import pt.ul.fc.css.tascaeats.domain.pagamento.Pagamento;
import pt.ul.fc.css.tascaeats.domain.utilizador.Morada;

@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate data;
    @Column(nullable = false)
    private LocalTime hora;
    @Column(name = "valor_total", nullable = false)
    private double valorTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPedido estado;

    // Espelho read-only vindo do microserviço de entregas.
    @Column(name = "entregador_id")
    private Long entregadorId;

    // Estado lido de forma eventual, sem lógica de transição no monólito.
    @Column(name = "estado_entrega_read_only")
    private String estadoEntregaReadOnly;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "entrega_rua")
    private String entregaRua;

    @Column(name = "entrega_numero")
    private Integer entregaNumero;

    @Column(name = "entrega_complemento")
    private String entregaComplemento;

    @Column(name = "entrega_pais")
    private String entregaPais;

    @Column(name = "entrega_distrito")
    private String entregaDistrito;

    @Column(name = "entrega_cidade")
    private String entregaCidade;

    @Column(name = "entrega_codigo_postal")
    private String entregaCodigoPostal;

    //@Column(name = "restaurante_id")
    //private Long restauranteId;

    @ElementCollection
    @CollectionTable(name = "pedido_produtos", joinColumns = @JoinColumn(name = "pedido_id"))
    @Column(name = "produto_id")
    private List<Long> produtosIds = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "pedido_restaurantes", joinColumns = @JoinColumn(name = "pedido_id"))
    @Column(name = "restaurante_id")
    private List<Long> restaurantesIds = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "pagamento_id")
    private Pagamento pagamento;

    @Column(name = "tipo_pagamento_solicitado")
    private String tipoPagamentoSolicitado;

    @ElementCollection
    @CollectionTable(name = "pedido_pagamento_detalhes", joinColumns = @JoinColumn(name = "pedido_id"))
    @jakarta.persistence.MapKeyColumn(name = "campo")
    @Column(name = "valor")
    private Map<String, String> detalhesPagamentoSolicitado = new LinkedHashMap<>();

    public Pedido() {
    }

    public Pedido(Long id, Long clientId) {
        this.id = id;
        this.clienteId = clientId;
        this.estado = EstadoPedido.CRIADO;
        this.data = LocalDate.now();
        this.hora = LocalTime.now();
    }

    public Pedido(Long id, Long clientId, Long restauranteId) {
        this(id, clientId);
        if (restauranteId != null) {
            this.restaurantesIds.add(restauranteId);
        }
    }

// ################### MÉTODOS DE NEGÓCIO ######################################################################

    public void adicionarProduto(Long produtoId, Long restauranteId){
        produtosIds.add(produtoId);
        if(!restaurantesIds.contains(restauranteId)){
            restaurantesIds.add(restauranteId);
        }
    }

    public void confirmarPagamento() {
        if (this.estado != EstadoPedido.CRIADO) {
            throw new IllegalStateException("O pedido só pode ser pago se estiver no estado CRIADO.");
        }
        this.estado = EstadoPedido.PAGO;
    }

    public void iniciarPreparacao() {
        if (this.estado != EstadoPedido.PAGO) {
            throw new IllegalStateException("O pedido precisa de ser pago antes de ser preparado.");
        }
        this.estado = EstadoPedido.A_PREPARAR;
    }

    public void marcarComoPronto() {
        if (this.estado != EstadoPedido.A_PREPARAR) {
            throw new IllegalStateException("O pedido não estava em preparação.");
        }
        this.estado = EstadoPedido.PRONTO_PARA_ENTREGA;
    }

    public void atribuirEntregador() {
        if (this.estado != EstadoPedido.PRONTO_PARA_ENTREGA) {
            throw new IllegalStateException("O pedido tem de estar pronto para poder ser atribuído a um entregador.");
        }
        this.estado = EstadoPedido.ATRIBUIDO_ENTREGADOR;
    }

    public void marcarComoEntregue() {
        if (this.estado != EstadoPedido.ATRIBUIDO_ENTREGADOR) {
            throw new IllegalStateException("O pedido não pode ser entregue se não tiver um entregador atribuído.");
        }
        this.estado = EstadoPedido.ENTREGUE;
    }

    public void cancelar() {
        if (this.estado == EstadoPedido.A_PREPARAR || 
            this.estado == EstadoPedido.PRONTO_PARA_ENTREGA || 
            this.estado == EstadoPedido.ATRIBUIDO_ENTREGADOR || 
            this.estado == EstadoPedido.ENTREGUE) {
            
            throw new IllegalStateException("Não é possível cancelar um pedido que já está em preparo ou a caminho.");
        }
        this.estado = EstadoPedido.CANCELADO;
    }


// ###################### GETTERS e SETTERS #####################################################################

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(double valorTotal) {
        this.valorTotal = valorTotal;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public void setEstado(EstadoPedido estado) {
        this.estado = estado;
    }

    public Long getEntregadorId() {
        return entregadorId;
    }

    public void setEntregadorId(Long entregadorId) {
        this.entregadorId = entregadorId;
    }

    public String getEstadoEntregaReadOnly() {
        return estadoEntregaReadOnly;
    }

    public void setEstadoEntregaReadOnly(String estadoEntregaReadOnly) {
        this.estadoEntregaReadOnly = estadoEntregaReadOnly;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public Long getclienteId() {
        return getClienteId();
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public void setMoradaEntrega(Morada morada) {
        if (morada == null) {
            return;
        }
        this.entregaRua = morada.getRua();
        this.entregaNumero = morada.getNumero();
        this.entregaComplemento = morada.getComplemento();
        this.entregaPais = morada.getPais();
        this.entregaDistrito = morada.getDistrito();
        this.entregaCidade = morada.getCidade();
        this.entregaCodigoPostal = morada.getCodigoPostal();
    }

    public Map<String, Object> getMoradaEntrega() {
        Map<String, Object> morada = new LinkedHashMap<>();
        morada.put("rua", entregaRua);
        morada.put("numero", entregaNumero);
        morada.put("complemento", entregaComplemento);
        morada.put("pais", entregaPais);
        morada.put("distrito", entregaDistrito);
        morada.put("cidade", entregaCidade);
        morada.put("codigoPostal", entregaCodigoPostal);
        return morada;
    }

    public String getEnderecoEntregaFormatado() {
        if (entregaRua == null || entregaRua.isBlank()) {
            return null;
        }
        return String.join(", ",
                valorOuVazio(entregaRua) + " " + (entregaNumero == null ? "" : entregaNumero),
                valorOuVazio(entregaCodigoPostal),
                valorOuVazio(entregaCidade),
                valorOuVazio(entregaDistrito),
                valorOuVazio(entregaPais)
        ).replaceAll("(,\\s*)+", ", ").replaceAll("^,\\s*|,\\s*$", "");
    }

    private String valorOuVazio(String valor) {
        return valor == null ? "" : valor.trim();
    }

    public List<Long> getProdutosIds() {
        return produtosIds;
    }

    public void setProdutosIds(List<Long> produtosIds) {
        this.produtosIds = produtosIds;
    }

    public List<Long> getRestaurantesIds() { return restaurantesIds;}

    public Pagamento getPagamento() {
        return pagamento;
    }

    public void setPagamento(Pagamento pagamento) {
        this.pagamento = pagamento;
    }

    public String getTipoPagamentoSolicitado() {
        return tipoPagamentoSolicitado;
    }

    public void setTipoPagamentoSolicitado(String tipoPagamentoSolicitado) {
        this.tipoPagamentoSolicitado = tipoPagamentoSolicitado;
    }

    public Map<String, String> getDetalhesPagamentoSolicitado() {
        return detalhesPagamentoSolicitado;
    }

    public void setDetalhesPagamentoSolicitado(Map<String, String> detalhesPagamentoSolicitado) {
        this.detalhesPagamentoSolicitado = detalhesPagamentoSolicitado != null
                ? new LinkedHashMap<>(detalhesPagamentoSolicitado)
                : new LinkedHashMap<>();
    }

    public boolean entregaEstaConcluida() {
        if (estadoEntregaReadOnly != null) {
            return "ENTREGUE".equalsIgnoreCase(estadoEntregaReadOnly);
        }
        return this.estado == EstadoPedido.ENTREGUE;
    }
}
