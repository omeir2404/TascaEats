// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.service.pedidoService;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import pt.ul.fc.css.tascaeats.domain.pedido.EstadoPedido;
import pt.ul.fc.css.tascaeats.domain.pedido.Pedido;
import pt.ul.fc.css.tascaeats.domain.produto.Produto;
import pt.ul.fc.css.tascaeats.domain.restaurante.Restaurante;
import pt.ul.fc.css.tascaeats.domain.utilizador.Cliente;
import pt.ul.fc.css.tascaeats.domain.utilizador.Morada;
import pt.ul.fc.css.tascaeats.dto.pedidoDTO.CriarPedidoDTO;
import pt.ul.fc.css.tascaeats.dto.pedidoDTO.ItemPedidoDTO;
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

        if (criarPedidoDto.getClienteId() == null || criarPedidoDto.getClienteId() <= 0) {
            throw new IllegalArgumentException("Cliente Id inválido.");
        }

        if (criarPedidoDto.getItens() == null || criarPedidoDto.getItens().isEmpty()) {
            throw new IllegalArgumentException("O pedido deve conter pelo menos um item.");
        }

        // Validar cliente
        var utilizador = utilizadorRepository.findById(criarPedidoDto.getClienteId())
            .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado: " + criarPedidoDto.getClienteId()));
        if (!(utilizador instanceof Cliente)) {
            throw new IllegalArgumentException("Apenas clientes podem criar pedidos.");
        }
        Cliente cliente = (Cliente) utilizador;

        // Validar morada: deve existir uma opção (ID de morada existente ou nova morada)
        if (criarPedidoDto.getMoradaId() == null && criarPedidoDto.getMoradaNova() == null) {
            throw new IllegalArgumentException("Deve ser fornecida uma morada de entrega (ID de morada existente ou dados de nova morada).");
        }

        if (criarPedidoDto.getMoradaId() != null && criarPedidoDto.getMoradaNova() != null) {
            throw new IllegalArgumentException("Apenas uma das opções deve ser fornecida: moradaId OU moradaNova.");
        }


        

        // Criar pedido
        Pedido pedido = new Pedido(null, criarPedidoDto.getClienteId());
        pedido.setValorTotal(valorTotal);

        // Adicionar produtos ao pedido
        for (ItemPedidoDTO item : criarPedidoDto.getItens()) {
            // Adicionar quantidade de vezes (ou ajustar a lógica conforme necessário)
            for (int i = 0; i < item.getQuantidade(); i++) {
                pedido.adicionarProduto(item.getProdutoId(), item.getRestauranteId());
            }
        }

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
