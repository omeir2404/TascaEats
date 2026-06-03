// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.service.pedidoService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import pt.ul.fc.css.tascaeats.repository.utilizadorRepository.IEntregadorRepository;
import pt.ul.fc.css.tascaeats.repository.utilizadorRepository.IUtilizadorRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PedidoService {

    private final IPedidoRepository pedidoRepository;
    private final IUtilizadorRepository utilizadorRepository;
    private final IRestauranteRepository restauranteRepository;
    private final IProdutoRepository produtoRepository;
    private final IEntregadorRepository entregadorRepository;

    public PedidoService(IPedidoRepository pedidoRepository, IUtilizadorRepository utilizadorRepository, IRestauranteRepository restauranteRepository, IProdutoRepository produtoRepository, IEntregadorRepository entregadorRepository) {
        this.pedidoRepository = pedidoRepository;
        this.utilizadorRepository = utilizadorRepository;
        this.restauranteRepository = restauranteRepository;
        this.produtoRepository = produtoRepository;
        this.entregadorRepository = entregadorRepository; 
        
    }

    public void criarPedido(CriarPedidoDTO criarPedidoDto){
        if (criarPedidoDto == null) {
            throw new IllegalArgumentException("Pedido invalido.");
        }

        if (criarPedidoDto.getClienteId() == null || criarPedidoDto.getClienteId() <= 0) {
            throw new IllegalArgumentException("Cliente Id invalido.");
        }

        List<ItemPedidoDTO> itens = criarPedidoDto.getItens();
        if (itens == null || itens.isEmpty()) {
            throw new IllegalArgumentException("O pedido deve conter pelo menos um produto.");
        }

        var utilizador = utilizadorRepository.findById(criarPedidoDto.getClienteId())
            .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado: " + criarPedidoDto.getClienteId()));
        if (!(utilizador instanceof Cliente)) {
            throw new IllegalArgumentException("Apenas clientes podem criar pedidos.");
        }
        Cliente cliente = (Cliente) utilizador;

        if (criarPedidoDto.getMoradaId() != null && criarPedidoDto.getMoradaNova() != null) {
            throw new IllegalArgumentException("Apenas uma das opcoes deve ser fornecida: moradaId OU moradaNova.");
        }

        if (criarPedidoDto.getMoradaId() != null) {
            boolean moradaValida = cliente.getAddresses().stream()
                .anyMatch(m -> m.getId().equals(criarPedidoDto.getMoradaId()));
            if (!moradaValida) {
                throw new IllegalArgumentException("Morada nao encontrada ou nao pertence ao cliente: " + criarPedidoDto.getMoradaId());
            }
        }

        if (criarPedidoDto.getMoradaNova() != null) {
            Morada nova = novaMoradaDeDTO(criarPedidoDto.getMoradaNova());
            cliente.addAdresses(nova);
            utilizadorRepository.save(cliente);
        }

        double valorTotal = 0;
        java.util.Set<Long> restaurantesValidados = new java.util.HashSet<>();
        java.util.Map<Long, Restaurante> restaurantesPorId = new java.util.HashMap<>();

        for (ItemPedidoDTO item : itens) {
            if (item.getProdutoId() == null || item.getProdutoId() <= 0) {
                throw new IllegalArgumentException("Produto Id invalido no item.");
            }

            if (item.getRestauranteId() == null || item.getRestauranteId() <= 0) {
                throw new IllegalArgumentException("Restaurante Id invalido no item.");
            }

            if (item.getQuantidade() == null || item.getQuantidade() <= 0) {
                throw new IllegalArgumentException("Quantidade invalida no item: deve ser maior que 0.");
            }

            Restaurante restaurante = restaurantesPorId.computeIfAbsent(item.getRestauranteId(), restauranteId ->
                    restauranteRepository.findById(restauranteId)
                    .orElseThrow(() -> new IllegalArgumentException("Restaurante nao encontrado: " + item.getRestauranteId()))
            );

            if (!restaurantesValidados.contains(item.getRestauranteId())) {
                restaurante.validarDisponibilidadeParaPedido();
                restaurantesValidados.add(item.getRestauranteId());
            }

            Produto produto = produtoRepository.findById(item.getProdutoId())
                    .orElseThrow(() -> new IllegalArgumentException("Produto nao encontrado: " + item.getProdutoId()));

            produto.validarDisponibilidadeParaPedido();

            boolean produtoDoMenuDoRestaurante = restaurante.getMenu() != null
                    && produto.getMenu() != null
                    && produto.getMenu().getId().equals(restaurante.getMenu().getId());
            boolean produtoLegadoDoRestaurante = produto.getMenu() == null
                    && produto.getRestaurante() != null
                    && produto.getRestaurante().getId() != null
                    && produto.getRestaurante().getId().equals(item.getRestauranteId());
            if (!produtoDoMenuDoRestaurante && !produtoLegadoDoRestaurante) {
                throw new IllegalArgumentException("O produto " + item.getProdutoId() + " nao pertence ao menu do restaurante " + item.getRestauranteId());
            }

            valorTotal += item.getQuantidade() * produto.getPreco();
        }

        Pedido pedido = new Pedido(null, criarPedidoDto.getClienteId());
        pedido.setValorTotal(valorTotal);
        pedido.setTipoPagamentoSolicitado(normalizarTipoPagamento(criarPedidoDto.getTipoPagamentoSolicitado()));
        pedido.setDetalhesPagamentoSolicitado(normalizarDetalhesPagamento(criarPedidoDto.getDetalhesPagamentoSolicitado()));

        for (ItemPedidoDTO item : itens) {
            for (int i = 0; i < item.getQuantidade(); i++) {
                pedido.adicionarProduto(item.getProdutoId(), item.getRestauranteId());
            }
        }

        pedidoRepository.save(pedido);
    }

    public void cancelarPedido(Long pedidoId){
        if (pedidoId == null) {
            throw new IllegalArgumentException("ID do pedido invalido.");
        }

        Optional<Pedido> pedidoOptional = pedidoRepository.findById(pedidoId);
        if (pedidoOptional.isEmpty()) {
            throw new IllegalArgumentException("Pedido nao encontrado: " + pedidoId);
        }
        Pedido pedido = pedidoOptional.get();
        if(pedido.getEstado() == EstadoPedido.CRIADO || pedido.getEstado() == EstadoPedido.PAGO){
            pedido.cancelar();
            pedidoRepository.save(pedido);
        }
        else{
            throw new IllegalStateException("Pedido no estado " + pedido.getEstado() + " nao pode ser cancelado.");
        }
    }

    @Transactional(readOnly = true)
    public List<Pedido> listarPedidos(Long clienteId, EstadoPedido estado) {
        List<Pedido> pedidos;
        if (clienteId != null && estado != null) {
            pedidos = pedidoRepository.findByClienteIdAndEstado(clienteId, estado);
        } else if (clienteId != null) {
            pedidos = pedidoRepository.findByClienteId(clienteId);
        } else if (estado != null) {
            pedidos = pedidoRepository.findByEstado(estado);
        } else {
            pedidos = pedidoRepository.findAll();
        }

        pedidos.forEach(this::inicializarColecoesPedido);
        return pedidos;
    }

    private void inicializarColecoesPedido(Pedido pedido) {
        pedido.getRestaurantesIds().size();
        pedido.getProdutosIds().size();
        pedido.getDetalhesPagamentoSolicitado().size();
    }

    public void atualizarEstadoPedido(Long pedidoId, String novoEstado) {
        if (pedidoId == null) {
            throw new IllegalArgumentException("ID do pedido invalido.");
        }

        Optional<Pedido> pedidoOptional = pedidoRepository.findById(pedidoId);
        if (pedidoOptional.isEmpty()) {
            throw new IllegalArgumentException("Pedido nao encontrado: " + pedidoId);
        }

        Pedido pedido = pedidoOptional.get();
        if (novoEstado == null || novoEstado.isBlank()) {
            throw new IllegalArgumentException("Novo estado invalido.");
        }

        String estadoNorm = novoEstado.trim().toUpperCase();
        switch (estadoNorm) {
            case "A_PREPARAR" -> pedido.iniciarPreparacao();
            case "PRONTO_PARA_ENTREGA" -> pedido.marcarComoPronto();
            case "ATRIBUIDO_ENTREGADOR" -> pedido.atribuirEntregador();
            case "ENTREGUE" -> pedido.marcarComoEntregue();
            case "CANCELADO" -> pedido.cancelar();
            default -> throw new IllegalArgumentException("Estado do pedido invalido: " + novoEstado);
        }

        pedidoRepository.save(pedido);
    }

    public void aplicarAtualizacaoEntrega(Long pedidoId, Long entregadorId, String estadoPedido, String estadoEntrega) {
        if (pedidoId == null) {
            throw new IllegalArgumentException("ID do pedido invalido.");
        }

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido nao encontrado: " + pedidoId));

        if (entregadorId != null) {
            pedido.setEntregadorId(entregadorId);

            // Procura diretamente no repositório de entregadores para contornar o Proxy do Hibernate
            entregadorRepository.findById(entregadorId).ifPresent(entregador -> {
                // Se o estado for ENTREGUE, liberta o estafeta, caso contrário (inclusive A_CAMINHO) fica ocupado
                if ("ENTREGUE".equalsIgnoreCase(estadoPedido) || "ENTREGUE".equalsIgnoreCase(estadoEntrega)) {
                    entregador.setAvailable(true);
                } else {
                    entregador.setAvailable(false);
                }
                entregadorRepository.save(entregador);
            });
        }
        
        if (estadoEntrega != null && !estadoEntrega.isBlank()) {
            pedido.setEstadoEntregaReadOnly(estadoEntrega.trim().toUpperCase());
        }
        if (estadoPedido != null && !estadoPedido.isBlank()) {
            pedido.setEstado(EstadoPedido.valueOf(estadoPedido.trim().toUpperCase()));
        }

        pedidoRepository.save(pedido);
    }

    private Morada novaMoradaDeDTO(pt.ul.fc.css.tascaeats.dto.moradaDTO.MoradaDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("A morada e obrigatoria.");
        }

        String rua = dto.getRua();
        if (rua == null || rua.isBlank()) {
            throw new IllegalArgumentException("A rua da morada e obrigatoria.");
        }

        Integer numero = dto.getNumero();
        String complemento = dto.getComplemento() != null ? dto.getComplemento().trim() : null;
        String pais = dto.getPais();
        if (pais == null || pais.isBlank()) {
            throw new IllegalArgumentException("O pais e obrigatorio.");
        }
        String distrito = dto.getDistrito();
        if (distrito == null || distrito.isBlank()) {
            throw new IllegalArgumentException("O distrito e obrigatorio.");
        }
        String cidade = dto.getCidade();
        if (cidade == null || cidade.isBlank()) {
            throw new IllegalArgumentException("A cidade e obrigatoria.");
        }
        String codigoPostal = dto.getCodigoPostal();
        if (codigoPostal == null || codigoPostal.isBlank()) {
            throw new IllegalArgumentException("O codigo postal e obrigatorio.");
        }

        if (numero == null || numero <= 0) {
            throw new IllegalArgumentException("O numero da porta nao pode ser negativo e tem de existir.");
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

    private String normalizarTipoPagamento(String tipoPagamento) {
        if (tipoPagamento == null || tipoPagamento.isBlank()) {
            return null;
        }
        String normalizado = tipoPagamento.trim().toUpperCase();
        return switch (normalizado) {
            case "MBWAY", "MULTIBANCO", "DINHEIRO", "CARTAO" -> normalizado;
            default -> throw new IllegalArgumentException("Tipo de pagamento nao suportado: " + normalizado);
        };
    }

    private Map<String, String> normalizarDetalhesPagamento(Map<String, String> detalhes) {
        Map<String, String> normalizados = new LinkedHashMap<>();
        if (detalhes == null) {
            return normalizados;
        }
        detalhes.forEach((campo, valor) -> {
            if (campo != null && !campo.isBlank() && valor != null && !valor.isBlank()) {
                normalizados.put(campo.trim(), valor.trim());
            }
        });
        return normalizados;
    }
}
