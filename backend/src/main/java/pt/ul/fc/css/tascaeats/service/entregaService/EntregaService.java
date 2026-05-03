// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.service.entregaService;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pt.ul.fc.css.tascaeats.domain.entrega.Entrega;
import pt.ul.fc.css.tascaeats.domain.pedido.EstadoPedido;
import pt.ul.fc.css.tascaeats.domain.pedido.Pedido;
import pt.ul.fc.css.tascaeats.domain.utilizador.Entregador;
import pt.ul.fc.css.tascaeats.repository.entregaRepository.IEntregaRepository;
import pt.ul.fc.css.tascaeats.repository.pedidoRepository.IPedidoRepository;
import pt.ul.fc.css.tascaeats.repository.utilizadorRepository.IUtilizadorRepository;

@Service
public class EntregaService {

    private static final String ENDERECO_ENTREGA_PENDENTE = "ENDERECO_NAO_INFORMADO";

    private final IEntregaRepository entregaRepository;
    private final IUtilizadorRepository utilizadorRepository;
    private final IPedidoRepository pedidoRepository;

    public EntregaService(
            IEntregaRepository entregaRepository,
            IUtilizadorRepository utilizadorRepository,
            IPedidoRepository pedidoRepository) {
        this.entregaRepository = entregaRepository;
        this.utilizadorRepository = utilizadorRepository;
        this.pedidoRepository = pedidoRepository;
    }

    @Transactional
    public Pedido iniciarPreparacao(Long pedidoId) {
        if (pedidoId == null) {
            throw new IllegalArgumentException("ID do pedido inválido.");
        }

        Optional<Pedido> pedidoOptional = pedidoRepository.findById(pedidoId);
        if (pedidoOptional.isEmpty()) {
            throw new IllegalArgumentException("Pedido não encontrado: " + pedidoId);
        }

        Pedido pedido = pedidoOptional.get();
        pedido.iniciarPreparacao();
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido simularPreparacaoRestauranteEAtribuirEntregador(Long pedidoId) {
        if (pedidoId == null) {
            throw new IllegalArgumentException("ID do pedido inválido.");
        }

        Optional<Pedido> pedidoOptional = pedidoRepository.findById(pedidoId);
        if (pedidoOptional.isEmpty()) {
            throw new IllegalArgumentException("Pedido não encontrado: " + pedidoId);
        }

        Pedido pedido = pedidoOptional.get();
        if (pedido.getEstado() == EstadoPedido.PAGO) {
            pedido.iniciarPreparacao();
            pedido.marcarComoPronto();
        } else if (pedido.getEstado() == EstadoPedido.A_PREPARAR) {
            pedido.marcarComoPronto();
        } else if (pedido.getEstado() == EstadoPedido.CRIADO) {
            pedido.confirmarPagamento();
            pedido.iniciarPreparacao();
            pedido.marcarComoPronto();
        }

        pedido = pedidoRepository.save(pedido);
        associarEntregadorAutomaticamente(pedido);
        return pedido;
    }

    
@Transactional
    public Pedido marcarPedidoComoPronto(Long pedidoId) {
        if (pedidoId == null) {
            throw new IllegalArgumentException("ID do pedido inválido.");
        }

        Optional<Pedido> pedidoOptional = pedidoRepository.findById(pedidoId);
        if (pedidoOptional.isEmpty()) {
            throw new IllegalArgumentException("Pedido não encontrado: " + pedidoId);
        }

        Pedido pedido = pedidoOptional.get();
        
        // 1. Mark the order as ready
        pedido.marcarComoPronto();
        pedido = pedidoRepository.save(pedido);

        // 2. Automatically assign a delivery person
        associarEntregadorAutomaticamente(pedido);

        return pedido;
    }

    private void associarEntregadorAutomaticamente(Pedido pedido) {
        Entregador entregador = null;
        
        // Find the first available delivery person.
        for (var utilizador : utilizadorRepository.findAll()) {
            if (utilizador instanceof Entregador candidato) {
                if (candidato.isAvailable() && !entregaRepository.existsByEntregadorIdAndHoraEntregaIsNull(candidato.getId())) {
                    entregador = candidato;
                    break;
                }
            }
        }

        if (entregador == null) {
            throw new IllegalStateException("Não há entregadores disponíveis de momento para o pedido: " + pedido.getId());
        }

        Entrega entrega = new Entrega(pedido, entregador.getId(), ENDERECO_ENTREGA_PENDENTE);

        entregador.setAvailable(false);

        if (pedido.getEstado() != EstadoPedido.ATRIBUIDO_ENTREGADOR) {
            pedido.atribuirEntregador();
        }

        utilizadorRepository.save(entregador);
        pedidoRepository.save(pedido);
        entregaRepository.save(entrega);
    }


    @Transactional
    public Entrega registarRecolha(Long pedidoId) {
        if (pedidoId == null) {
            throw new IllegalArgumentException("ID do pedido inválido.");
        }

        Optional<Entrega> entregaOptional = entregaRepository.findByPedidoId(pedidoId);
        if (entregaOptional.isEmpty()) {
            throw new IllegalArgumentException("Entrega não encontrada para pedido: " + pedidoId);
        }
        Entrega entrega = entregaOptional.get();

        Optional<Pedido> pedidoOptional = pedidoRepository.findById(pedidoId);
        if (pedidoOptional.isEmpty()) {
            throw new IllegalArgumentException("Pedido não encontrado: " + pedidoId);
        }

        Pedido pedido = pedidoOptional.get();

        if (pedido.getEstado() != EstadoPedido.ATRIBUIDO_ENTREGADOR) {
            throw new IllegalStateException("Só é possível registar recolha após atribuição de entregador.");
        }

        entrega.registarRecolha();

        return entregaRepository.save(entrega);
    }

    @Transactional
    public Entrega registarEntregaFinal(Long pedidoId) {
        if (pedidoId == null) {
            throw new IllegalArgumentException("ID do pedido inválido.");
        }

        Optional<Entrega> entregaOptional = entregaRepository.findByPedidoId(pedidoId);
        if (entregaOptional.isEmpty()) {
            throw new IllegalArgumentException("Entrega não encontrada para pedido: " + pedidoId);
        }
        Entrega entrega = entregaOptional.get();

        Optional<Pedido> pedidoOptional = pedidoRepository.findById(pedidoId);
        if (pedidoOptional.isEmpty()) {
            throw new IllegalArgumentException("Pedido não encontrado: " + pedidoId);
        }

        Pedido pedido = pedidoOptional.get();

        Long entregadorId = entrega.getEntregadorId();
        if (entregadorId == null) {
            throw new IllegalArgumentException("Entregador não encontrado: null");
        }

        Optional<pt.ul.fc.css.tascaeats.domain.utilizador.Utilizador> entregadorOptional = utilizadorRepository.findById(entregadorId);
        if (entregadorOptional.isEmpty() || !(entregadorOptional.get() instanceof Entregador)) {
            throw new IllegalArgumentException("Entregador não encontrado: " + entrega.getEntregadorId());
        }
        Entregador entregador = (Entregador) entregadorOptional.get();

        entrega.registarConclusao();
        pedido.marcarComoEntregue();

        entregador.setAvailable(true);

        utilizadorRepository.save(entregador);
        pedidoRepository.save(pedido);
        return entregaRepository.save(entrega);
    }
}