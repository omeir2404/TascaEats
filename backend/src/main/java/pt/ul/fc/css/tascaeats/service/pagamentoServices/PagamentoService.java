// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.service.pagamentoServices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import pt.ul.fc.css.tascaeats.domain.pagamento.Pagamento;
import pt.ul.fc.css.tascaeats.domain.pedido.EstadoPedido;
import pt.ul.fc.css.tascaeats.domain.pedido.Pedido;
import pt.ul.fc.css.tascaeats.domain.utilizador.Morada;
import pt.ul.fc.css.tascaeats.dto.pagamentoDTO.ProcessarPagamentoDTO;
import pt.ul.fc.css.tascaeats.events.PedidoPagoEvent;
import pt.ul.fc.css.tascaeats.publishers.PedidoPagoPublisher;
import pt.ul.fc.css.tascaeats.repository.pagamentoRepository.IPagamentoRepository;
import pt.ul.fc.css.tascaeats.repository.pedidoRepository.IPedidoRepository;
import pt.ul.fc.css.tascaeats.repository.utilizadorRepository.IClienteRepository;

import java.util.Optional;


@Service
public class PagamentoService {

    private final IPedidoRepository pedidoRepository;
    private final IPagamentoRepository pagamentoRepository;
    private final PagamentoFactory pagamentoFactory;
    private final IClienteRepository clienteRepository;
    private final PedidoPagoPublisher pedidoPagoPublisher;

    public PagamentoService(IPedidoRepository pedidoRepository,
                            IPagamentoRepository pagamentoRepository,
                            PagamentoFactory pagamentoFactory,
                            IClienteRepository clienteRepository,
                            PedidoPagoPublisher pedidoPagoPublisher) {
        this.pedidoRepository = pedidoRepository;
        this.pagamentoRepository = pagamentoRepository;
        this.pagamentoFactory = pagamentoFactory;
        this.clienteRepository = clienteRepository;
        this.pedidoPagoPublisher = pedidoPagoPublisher;
    }

    @Transactional
    public void processarPagamento(ProcessarPagamentoDTO dto) {
        Long pedidoId = dto.getPedidoId();
        if (pedidoId == null) {
            throw new IllegalArgumentException("Pedido ID é obrigatório");
        }
		Optional<Pedido> pedidoOpcional = pedidoRepository.findById(pedidoId);

		if (pedidoOpcional.isEmpty()) {
			throw new IllegalArgumentException("Pedido não encontrado");
		}

		Pedido pedido = pedidoOpcional.get();
        if (pedido.getEstado() != EstadoPedido.CRIADO) {
            throw new IllegalStateException("O pedido só pode ser pago se estiver no estado CRIADO.");
        }

        Pagamento pagamento = pagamentoFactory.create(dto.getTipoPagamento(), pedido.getValorTotal(), dto.getDetalhes());
        if (pagamento == null) {
            throw new IllegalArgumentException("Falha ao criar pagamento");
        }

        pagamentoRepository.save(pagamento);

        pedido.setPagamento(pagamento);
        pedido.confirmarPagamento();
        pedidoRepository.save(pedido);

        publicarDepoisDoCommit(pedido);
    }

    private void publicarDepoisDoCommit(Pedido pedido) {
        Runnable publicar = () -> {
            String endereco = clienteRepository.findById(pedido.getClienteId())
                    .map(cliente -> formatarMorada(cliente.getAddress()))
                    .orElse("ENDERECO_NAO_INFORMADO");
            pedidoPagoPublisher.publicar(new PedidoPagoEvent(
                    pedido.getId(),
                    pedido.getClienteId(),
                    endereco
            ));
        };

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            publicar.run();
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                publicar.run();
            }
        });
    }

    private String formatarMorada(Morada morada) {
        if (morada == null) {
            return "ENDERECO_NAO_INFORMADO";
        }
        return String.join(", ",
                valorOuVazio(morada.getRua()) + " " + (morada.getNumero() == null ? "" : morada.getNumero()),
                valorOuVazio(morada.getCodigoPostal()),
                valorOuVazio(morada.getCidade()),
                valorOuVazio(morada.getDistrito()),
                valorOuVazio(morada.getPais())
        ).replaceAll("(,\\s*)+", ", ").replaceAll("^,\\s*|,\\s*$", "");
    }

    private String valorOuVazio(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
