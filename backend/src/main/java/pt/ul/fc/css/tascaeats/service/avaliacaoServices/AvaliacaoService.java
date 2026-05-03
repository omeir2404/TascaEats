// ##############################################################
// 					  TASCAEATS								#
// 					CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.service.avaliacaoServices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pt.ul.fc.css.tascaeats.domain.avaliacao.Avaliacao;
import pt.ul.fc.css.tascaeats.domain.pedido.EstadoPedido;
import pt.ul.fc.css.tascaeats.domain.pedido.Pedido;
import pt.ul.fc.css.tascaeats.domain.restaurante.Restaurante;
import pt.ul.fc.css.tascaeats.domain.utilizador.Cliente;
import pt.ul.fc.css.tascaeats.domain.utilizador.Utilizador;
import pt.ul.fc.css.tascaeats.dto.avaliacaoDTO.AvaliacaoDTO;
import pt.ul.fc.css.tascaeats.repository.avaliacaoRepository.IAvaliacaoRepository;
import pt.ul.fc.css.tascaeats.repository.pedidoRepository.IPedidoRepository;
import pt.ul.fc.css.tascaeats.repository.restauranteRepository.IRestauranteRepository;
import pt.ul.fc.css.tascaeats.repository.utilizadorRepository.IUtilizadorRepository;

@Service
public class AvaliacaoService {

    private final IAvaliacaoRepository avaliacaoRepository;
    private final IPedidoRepository pedidoRepository;
    private final IRestauranteRepository restauranteRepository;
    private final IUtilizadorRepository utilizadorRepository;

    public AvaliacaoService(
            IAvaliacaoRepository avaliacaoRepository,
            IPedidoRepository pedidoRepository,
            IRestauranteRepository restauranteRepository,
            IUtilizadorRepository utilizadorRepository) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.pedidoRepository = pedidoRepository;
        this.restauranteRepository = restauranteRepository;
        this.utilizadorRepository = utilizadorRepository;
    }

    public AvaliacaoDTO getAvaliacaoByPedido(Long pedidoId) {
        if (pedidoId == null) {
            return null;
        }
        return avaliacaoRepository.findByPedidoId(pedidoId)
                .map(a -> new AvaliacaoDTO(
                        a.getId(),
                        a.getClassificacao(),
                        a.getComentario(),
                        a.getCliente().getId(),
                        a.getRestaurante().getId(),
                        a.getPedido().getId()
                ))
                .orElse(null);
    }

    @Transactional
    public Avaliacao criarAvaliacao(AvaliacaoDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Dados da avaliação são obrigatórios.");
        }

        if (dto.getClassificacao() < 1 || dto.getClassificacao() > 5) {
            throw new IllegalArgumentException("A classificação deve estar entre 1 e 5.");
        }

        if (dto.getPedidoId() == null || dto.getClienteId() == null || dto.getRestauranteId() == null) {
            throw new IllegalArgumentException("Pedido, cliente e restaurante são obrigatórios.");
        }

        if (avaliacaoRepository.existsByPedidoId(dto.getPedidoId())) {
            throw new IllegalStateException("Este pedido já foi avaliado.");
        }

        Pedido pedido = pedidoRepository.findById(dto.getPedidoId())
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado: " + dto.getPedidoId()));

        if (pedido.getEstado() != EstadoPedido.ENTREGUE) {
            throw new IllegalStateException("Só é possível avaliar pedidos entregues.");
        }

        if (!dto.getClienteId().equals(pedido.getClienteId())) {
            throw new IllegalArgumentException("O cliente não corresponde ao pedido informado.");
        }

        if (pedido.getRestaurantesIds() == null || !pedido.getRestaurantesIds().contains(dto.getRestauranteId())) {
            throw new IllegalArgumentException("O restaurante informado não pertence ao pedido.");
        }

        Utilizador utilizador = utilizadorRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado: " + dto.getClienteId()));
        if (!(utilizador instanceof Cliente cliente)) {
            throw new IllegalArgumentException("O utilizador informado não é cliente.");
        }

        Restaurante restaurante = restauranteRepository.findById(dto.getRestauranteId())
                .orElseThrow(() -> new IllegalArgumentException("Restaurante não encontrado: " + dto.getRestauranteId()));

        String comentario = dto.getComentario() == null ? "" : dto.getComentario().trim();
        Avaliacao avaliacao = new Avaliacao(dto.getClassificacao(), comentario, cliente, restaurante, pedido);
        
        Avaliacao saved = avaliacaoRepository.save(avaliacao);

        atualizarMediaRestaurante(restaurante);
        restauranteRepository.save(restaurante);

        return saved;
    }

    public boolean pedidoJaAvaliado(Long pedidoId) {
        if (pedidoId == null) {
            return false;
        }
        return avaliacaoRepository.existsByPedidoId(pedidoId);
    }

    private void atualizarMediaRestaurante(Restaurante restaurante) {
        Long restId = restaurante.getId();
        
        // Let the database do the heavy lifting!
        Integer totalAvaliacoes = avaliacaoRepository.countByRestauranteId(restId);
        Double media = avaliacaoRepository.calculateAverageRatingByRestauranteId(restId);

        // Set the total (default to 0 if null)
        restaurante.setNumeroAvaliacoes(totalAvaliacoes != null ? totalAvaliacoes : 0);
        
        // Set the average and round it to 1 decimal place (e.g., 4.56666 -> 4.6)
        if (media != null) {
            double roundedMedia = Math.round(media * 10.0) / 10.0;
            restaurante.setAvaliacaoMedia(roundedMedia);
        } else {
            restaurante.setAvaliacaoMedia(0.0);
        }
    }

    public java.util.List<java.util.Map<String, Object>> getAvaliacoesRestaurante(Long restauranteId) {
        return avaliacaoRepository.findByRestauranteId(restauranteId).stream().map(a -> {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("classificacao", a.getClassificacao());
            map.put("comentario", a.getComentario());
            map.put("clienteId", a.getCliente().getId()); 
            return map;
        }).toList();
    }
}
