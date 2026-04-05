package pt.ul.fc.css.tascaeats.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;



public class Pedido {

    private Long id;

    private LocalDate data;
    private LocalTime hora;
    private double valorTotal;

    private EstadoPedido estado;

    private Long clienteId;
    private Long restauranteId;

    private List<Long> produtosIds = new ArrayList<>();

    private Pagamento pagamento;

    public Pedido(Long id, Long clientId, Long restauranteId) {
        this.id = id;
        this.clienteId = clientId;
        this.restauranteId = restauranteId;
    }

// ###################### Gestão de Itens ######################################################################

    public void recalcularValorTotal(){
        for(Long itemId : this.produtosIds){
            // get valor do produto de catalogoProdutos (ex CatalogoProduto.getItemValue(itemId))
            valorTotal += itemId;// INCORRETO SO PARA A FUNCAO NAO RECLAMAR, devia ser o valor do item nao o ID
        }
    }

    public void adicionarProduto(Long produtoID){
        this.produtosIds.add(produtoID);
        recalcularValorTotal();
    }

    public void removerProduto(Long produtoID){
        this.produtosIds.remove(produtoID);
        recalcularValorTotal();
    }


// ###################### Estados e regras de negocio ###########################################################

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

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public Long getRestauranteId() {
        return restauranteId;
    }

    public void setRestauranteId(Long restauranteId) {
        this.restauranteId = restauranteId;
    }

    public List<Long> getProdutosIds() {
        return produtosIds;
    }

    public void setProdutosIds(List<Long> produtosIds) {
        this.produtosIds = produtosIds;
    }

    public Pagamento getPagamento() {
        return pagamento;
    }

    public void setPagamento(Pagamento pagamento) {
        this.pagamento = pagamento;
    }
}
