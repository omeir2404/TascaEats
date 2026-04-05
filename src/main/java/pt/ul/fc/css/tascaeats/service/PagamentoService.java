package pt.ul.fc.css.tascaeats.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import pt.ul.fc.css.tascaeats.domain.Pagamento;
import pt.ul.fc.css.tascaeats.domain.Pedido;
import pt.ul.fc.css.tascaeats.dto.ProcessarPagamentoDTO;
import pt.ul.fc.css.tascaeats.repository.IPagamentoRepository;
import pt.ul.fc.css.tascaeats.repository.IPedidoRepository;

@Service
public class PagamentoService {

    private final IPedidoRepository pedidoRepository;
    private final IPagamentoRepository pagamentoRepository;
    private final PagamentoFactory pagamentoFactory;

    public PagamentoService(IPedidoRepository pedidoRepository, IPagamentoRepository pagamentoRepository, PagamentoFactory pagamentoFactory) {
        this.pedidoRepository = pedidoRepository;
        this.pagamentoRepository = pagamentoRepository;
        this.pagamentoFactory = pagamentoFactory;
    }

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

        Pagamento pagamento = pagamentoFactory.create(dto.getTipoPagamento(), pedido.getValorTotal(), dto.getDetalhes());
        if (pagamento == null) {
            throw new IllegalArgumentException("Falha ao criar pagamento");
        }

        pagamentoRepository.save(pagamento);

        pedido.setPagamento(pagamento);
        pedido.confirmarPagamento();
        pedidoRepository.save(pedido);
    }
}
