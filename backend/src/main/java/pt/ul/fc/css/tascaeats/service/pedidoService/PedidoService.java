// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.service.pedidoService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import pt.ul.fc.css.tascaeats.domain.pedido.EstadoPedido;
import pt.ul.fc.css.tascaeats.domain.pedido.Pedido;
import pt.ul.fc.css.tascaeats.domain.produto.Produto;
import pt.ul.fc.css.tascaeats.domain.restaurante.Restaurante;
import pt.ul.fc.css.tascaeats.domain.utilizador.Cliente;
import pt.ul.fc.css.tascaeats.domain.utilizador.Entregador;
import pt.ul.fc.css.tascaeats.domain.utilizador.Morada;
import pt.ul.fc.css.tascaeats.domain.utilizador.Utilizador;
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
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${delivery.api.url:${DELIVERY_API_URL:http://localhost:80}}")
    private String deliveryApiUrl;

    public PedidoService(IPedidoRepository pedidoRepository, IUtilizadorRepository utilizadorRepository, IRestauranteRepository restauranteRepository, IProdutoRepository produtoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.utilizadorRepository = utilizadorRepository;
        this.restauranteRepository = restauranteRepository;
        this.produtoRepository = produtoRepository;
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

            Produto produto = produtoRepository.findById(item.getProdutoId())
                .orElseThrow(() -> new IllegalArgumentException("Produto nao encontrado: " + item.getProdutoId()));

            produto.validarDisponibilidadeParaPedido();

            if (produto.getRestaurante() == null || !produto.getRestaurante().getId().equals(item.getRestauranteId())) {
                throw new IllegalArgumentException("O produto " + item.getProdutoId() + " nao pertence ao restaurante " + item.getRestauranteId());
            }

            if (!restaurantesValidados.contains(item.getRestauranteId())) {
                Restaurante restaurante = restauranteRepository.findById(item.getRestauranteId())
                    .orElseThrow(() -> new IllegalArgumentException("Restaurante nao encontrado: " + item.getRestauranteId()));

                restaurante.validarDisponibilidadeParaPedido();
                restaurantesValidados.add(item.getRestauranteId());
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
            case "PRONTO_PARA_ENTREGA" -> {
                pedido.marcarComoPronto();
                atribuirEntregadorAutomaticamente(pedido);
            }
            case "ATRIBUIDO_ENTREGADOR" -> atribuirEntregadorAutomaticamente(pedido);
            case "ENTREGUE" -> pedido.marcarComoEntregue();
            case "CANCELADO" -> pedido.cancelar();
            default -> throw new IllegalArgumentException("Estado do pedido invalido: " + novoEstado);
        }

        pedidoRepository.save(pedido);
    }

    private void atribuirEntregadorAutomaticamente(Pedido pedido) {
        if (pedido.getEstado() == EstadoPedido.ATRIBUIDO_ENTREGADOR && pedido.getEntregadorId() != null) {
            return;
        }
        if (pedido.getEstado() != EstadoPedido.PRONTO_PARA_ENTREGA) {
            throw new IllegalStateException("O pedido tem de estar pronto para poder ser atribuído a um entregador.");
        }

        Map<String, Object> entrega = criarOuObterEntrega(pedido.getId());
        Long backendEntregadorId = resolverEntregadorBackendId(entrega);

        pedido.atribuirEntregador();
        pedido.setEntregadorId(backendEntregadorId);
        pedido.setEstadoEntregaReadOnly(Objects.toString(entrega.getOrDefault("estado", "ATRIBUIDA"), "ATRIBUIDA"));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> criarOuObterEntrega(Long pedidoId) {
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    deliveryBaseUrl() + "/entregas/atribuir",
                    Map.of("pedidoId", pedidoId),
                    Map.class);
            return response.getBody() == null ? Map.of() : response.getBody();
        } catch (HttpClientErrorException.Conflict e) {
            String body = e.getResponseBodyAsString();
            if (body != null && body.toLowerCase().contains("ja tem uma entrega")) {
                return obterEntregaExistente(pedidoId);
            }
            throw new IllegalStateException("Erro ao atribuir entregador: " + body);
        } catch (HttpClientErrorException e) {
            throw new IllegalStateException("Erro ao atribuir entregador: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new IllegalStateException("Erro ao contactar serviço de entregas: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> obterEntregaExistente(Long pedidoId) {
        try {
            Map<String, Object> entrega = restTemplate.getForObject(
                    deliveryBaseUrl() + "/entregas/pedido/" + pedidoId,
                    Map.class);
            return entrega == null ? Map.of() : entrega;
        } catch (RestClientException e) {
            throw new IllegalStateException("Pedido ja tem entrega, mas nao foi possivel consulta-la: " + e.getMessage());
        }
    }

    private Long resolverEntregadorBackendId(Map<String, Object> entrega) {
        String email = Objects.toString(entrega.get("entregadorEmail"), "");
        if (!email.isBlank()) {
            Optional<Utilizador> utilizador = utilizadorRepository.findByEmail(email);
            if (utilizador.isPresent()) {
                Utilizador backendUser = utilizador.get();
                if (backendUser instanceof Entregador entregador) {
                    entregador.setAvailable(false);
                    utilizadorRepository.save(entregador);
                }
                return backendUser.getId();
            }
        }

        Object entregadorId = entrega.get("entregadorId");
        if (entregadorId instanceof Number number) {
            return number.longValue();
        }
        if (entregadorId != null) {
            try {
                return Long.parseLong(entregadorId.toString());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private String deliveryBaseUrl() {
        String value = deliveryApiUrl == null || deliveryApiUrl.isBlank() ? "http://localhost:80" : deliveryApiUrl.trim();
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
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
