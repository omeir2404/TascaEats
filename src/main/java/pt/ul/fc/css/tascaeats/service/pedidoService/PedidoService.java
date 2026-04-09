package pt.ul.fc.css.tascaeats.service.pedidoService;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import pt.ul.fc.css.tascaeats.domain.pedido.EstadoPedido;
import pt.ul.fc.css.tascaeats.domain.pedido.Pedido;
import pt.ul.fc.css.tascaeats.domain.produto.Produto;
import pt.ul.fc.css.tascaeats.dto.pedidoDTO.CriarPedidoDTO;
import pt.ul.fc.css.tascaeats.repository.pedidoRepository.IPedidoRepository;
import pt.ul.fc.css.tascaeats.repository.produtoRepository.IProdutoRepository;
import pt.ul.fc.css.tascaeats.repository.restauranteRepository.IRestauranteRepository;
import pt.ul.fc.css.tascaeats.repository.utilizadorRepository.IUtilizadorRepository;

@Service
public class PedidoService {

    private final IPedidoRepository pedidoRepository;
    private final IUtilizadorRepository utilizadorRepository;
    private final IRestauranteRepository restauranteRepository;
    private final IProdutoRepository produtoRepository;

    public PedidoService(IPedidoRepository pedidoRepository, IUtilizadorRepository utilizadorRepository, IRestauranteRepository restauranteRepository, IProdutoRepository produtoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.utilizadorRepository = utilizadorRepository;
        this.restauranteRepository = restauranteRepository;
        this.produtoRepository = produtoRepository;
    }

    public void criarPedido(CriarPedidoDTO criarPedidoDto){
        if (criarPedidoDto == null) {
            throw new IllegalArgumentException("Pedido inválido.");
        }

        if (criarPedidoDto.getProdutosIds() == null || criarPedidoDto.getProdutosIds().isEmpty()) {
            throw new IllegalArgumentException("O pedido deve conter pelo menos um produto.");
        }

        if (utilizadorRepository.findById(criarPedidoDto.getClienteeId()).isEmpty()) {
            throw new IllegalArgumentException("Clientee não encontrado: " + criarPedidoDto.getClienteeId());
        }

        if (restauranteRepository.findById(criarPedidoDto.getRestauranteId()).isEmpty()) {
            throw new IllegalArgumentException("Restaurante não encontrado: " + criarPedidoDto.getRestauranteId());
        }

        double valorTotal = 0;

        for (Long produtoId : criarPedidoDto.getProdutosIds()) {
            Optional<Produto> produtoOptional  = produtoRepository.findById(produtoId);
            if (produtoOptional.isEmpty()) {
                throw new IllegalArgumentException("Produto não encontrado: " + produtoId);
            }
            var produto = produtoOptional.get();

            valorTotal += produto.getPreco();
        }

        // O Null é pq o Id vai ser gerado.
        Pedido pedido = new Pedido(null, criarPedidoDto.getClienteeId(), criarPedidoDto.getRestauranteId());
        pedido.setValorTotal(valorTotal);
        pedidoRepository.save(pedido);
    }

    public void cancelarPedido(Long pedidoId){
        if (pedidoId == null) {
            throw new IllegalArgumentException("ID do pedido inválido.");
        }

        Optional<Pedido> pedidoOptional = pedidoRepository.findById(pedidoId);
        if (pedidoOptional.isEmpty()) {
            throw new IllegalArgumentException("Pedido não encontrado: " + pedidoId);
        }
        Pedido pedido = pedidoOptional.get();
        if(pedido.getEstado() == EstadoPedido.CRIADO || pedido.getEstado() == EstadoPedido.PAGO){
            pedido.cancelar();
            pedidoRepository.save(pedido);
        }
        else{
            throw new IllegalStateException("Pedido no estado " + pedido.getEstado() + " não pode ser cancelado.");
        }
    }

    public List<Pedido> listarPedidos(Long clienteId, EstadoPedido estado) {
        if (clienteId != null && estado != null) {
            return pedidoRepository.findByClienteIdAndEstado(clienteId, estado);
        }

        if (clienteId != null) {
            return pedidoRepository.findByClienteId(clienteId);
        }

        if (estado != null) {
            return pedidoRepository.findByEstado(estado);
        }

        return pedidoRepository.findAll();
    }
}
