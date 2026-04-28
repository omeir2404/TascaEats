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

        // Se usar morada existente, validar que pertence ao cliente
        if (criarPedidoDto.getMoradaId() != null) {
            boolean moradaValida = cliente.getAddresses().stream()
                .anyMatch(m -> m.getId().equals(criarPedidoDto.getMoradaId()));
            if (!moradaValida) {
                throw new IllegalArgumentException("Morada não encontrada ou não pertence ao cliente: " + criarPedidoDto.getMoradaId());
            }
        }

        // Se for fornecida uma nova morada, criar e associar ao cliente (persistido ao salvar o cliente)
        if (criarPedidoDto.getMoradaNova() != null) {
            Morada nova = novaMoradaDeDTO(criarPedidoDto.getMoradaNova());
            cliente.addAdresses(nova);
            // Guardar a nova morada associada ao cliente
            utilizadorRepository.save(cliente);
        }

        double valorTotal = 0;
        java.util.Set<Long> restaurantesValidados = new java.util.HashSet<>();

        // Validar itens e calcular valor total
        for (ItemPedidoDTO item : criarPedidoDto.getItens()) {
            if (item.getProdutoId() == null || item.getProdutoId() <= 0) {
                throw new IllegalArgumentException("Produto Id inválido no item.");
            }

            if (item.getRestauranteId() == null || item.getRestauranteId() <= 0) {
                throw new IllegalArgumentException("Restaurante Id inválido no item.");
            }

            if (item.getQuantidade() == null || item.getQuantidade() <= 0) {
                throw new IllegalArgumentException("Quantidade inválida no item: deve ser maior que 0.");
            }

            // Validar produto
            Produto produto = produtoRepository.findById(item.getProdutoId())
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado: " + item.getProdutoId()));

            produto.validarDisponibilidadeParaPedido();

            // Verificar que o produto pertence ao restaurante indicado no item
            if (produto.getRestaurante() == null || !produto.getRestaurante().getId().equals(item.getRestauranteId())) {
                throw new IllegalArgumentException("O produto " + item.getProdutoId() + " não pertence ao restaurante " + item.getRestauranteId());
            }

            // Validar restaurante (uma única vez por restaurante)
            if (!restaurantesValidados.contains(item.getRestauranteId())) {
                Restaurante restaurante = restauranteRepository.findById(item.getRestauranteId())
                    .orElseThrow(() -> new IllegalArgumentException("Restaurante não encontrado: " + item.getRestauranteId()));

                restaurante.validarDisponibilidadeParaPedido();
                restaurantesValidados.add(item.getRestauranteId());
            }

            // Acumular valor total (quantidade * preço)
            valorTotal += item.getQuantidade() * produto.getPreco();
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

    // Converter MoradaDTO to domain Morada
    private Morada novaMoradaDeDTO(pt.ul.fc.css.tascaeats.dto.moradaDTO.MoradaDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("A morada é obrigatória.");
        }

        String rua = dto.getRua();
        if (rua == null || rua.isBlank()) {
            throw new IllegalArgumentException("A rua da morada é obrigatória.");
        }

        Integer numero = dto.getNumero();
        String complemento = dto.getComplemento() != null ? dto.getComplemento().trim() : null;
        String pais = dto.getPais();
        if (pais == null || pais.isBlank()) {
            throw new IllegalArgumentException("O país é obrigatório.");
        }
        String distrito = dto.getDistrito();
        if (distrito == null || distrito.isBlank()) {
            throw new IllegalArgumentException("O distrito é obrigatório.");
        }
        String cidade = dto.getCidade();
        if (cidade == null || cidade.isBlank()) {
            throw new IllegalArgumentException("A cidade é obrigatória.");
        }
        String codigoPostal = dto.getCodigoPostal();
        if (codigoPostal == null || codigoPostal.isBlank()) {
            throw new IllegalArgumentException("O código postal é obrigatório.");
        }

        if (numero == null || numero <= 0) {
            throw new IllegalArgumentException("O número da porta não pode ser negativo e tem de existir.");
        }

        Morada morada = new Morada();
        morada.setRua(rua.trim());
        morada.setNumero(numero);
        morada.setComplemento(complemento);
        morada.setPais(pais.trim());
        morada.setDistrito(distrito.trim());
        morada.setCidade(cidade.trim());
        morada.setCodigoPostal(codigoPostal.trim());

        return morada;
    }
}
