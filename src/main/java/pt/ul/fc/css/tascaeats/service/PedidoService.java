package pt.ul.fc.css.tascaeats.service;

import java.util.Optional;

import pt.ul.fc.css.tascaeats.domain.Pedido;
import pt.ul.fc.css.tascaeats.dto.CriarPedidoDTO;
import pt.ul.fc.css.tascaeats.repository.IPedidoRepository;
import pt.ul.fc.css.tascaeats.repository.IRestauranteRepository;
import pt.ul.fc.css.tascaeats.repository.IUtilizadorRepository;

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

        // validar cliente e restaurante
        if (utilizadorRepository.findById(criarPedidoDto.clienteId).isEmpty()) {
            throw new IllegalArgumentException("Cliente não encontrado: " + criarPedidoDto.clienteId);
        }

        if (restauranteRepository.findById(criarPedidoDto.restauranteId).isEmpty()) {
            throw new IllegalArgumentException("Restaurante não encontrado: " + criarPedidoDto.restauranteId);
        }

        //  Lógica: Calcular o valorTotal com base no preço dos produtos iterados.
        Long valorTotal = 0L;

        for (Long produtoId : criarPedidoDto.getProdutosIds()) {
            Optional<Produto> produtoOptional  = ProdutoRepository.findById(produtoId);
            if (produtoOptional.isEmpty()) {
                throw new IllegalArgumentException("Produto não encontrado: " + produtoId);
            }
            var produto = produtoOptional.get();

            valorTotal += produto.getPreco();
        }

        // O Null é pq o Id vai ser gerado.
        Pedido pedido = new Pedido(null, criarPedidoDto.getClienteId(), criarPedidoDto.getRestauranteId());
        pedidoRepository.save(pedido);


    }
}
