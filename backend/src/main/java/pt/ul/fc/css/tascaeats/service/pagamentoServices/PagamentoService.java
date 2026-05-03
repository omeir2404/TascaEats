// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.service.pagamentoServices;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pt.ul.fc.css.tascaeats.domain.pagamento.Pagamento;
import pt.ul.fc.css.tascaeats.domain.pedido.Pedido;
import pt.ul.fc.css.tascaeats.dto.pagamentoDTO.ProcessarPagamentoDTO;
import pt.ul.fc.css.tascaeats.repository.pagamentoRepository.IPagamentoRepository;
import pt.ul.fc.css.tascaeats.repository.pedidoRepository.IPedidoRepository;
import pt.ul.fc.css.tascaeats.service.entregaService.EntregaService;

@Service
public class PagamentoService {

    private final IPedidoRepository pedidoRepository;
    private final IPagamentoRepository pagamentoRepository;
    private final PagamentoFactory pagamentoFactory;
    private final EntregaService entregaService;

    public PagamentoService(IPedidoRepository pedidoRepository, IPagamentoRepository pagamentoRepository, PagamentoFactory pagamentoFactory, EntregaService entregaService) {
        this.pedidoRepository = pedidoRepository;
        this.pagamentoRepository = pagamentoRepository;
        this.pagamentoFactory = pagamentoFactory;
        this.entregaService = entregaService;
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

        Pagamento pagamento = pagamentoFactory.create(dto.getTipoPagamento(), pedido.getValorTotal(), dto.getDetalhes());
        if (pagamento == null) {
            throw new IllegalArgumentException("Falha ao criar pagamento");
        }

        pagamentoRepository.save(pagamento);

        pedido.setPagamento(pagamento);
        pedido.confirmarPagamento();
        pedidoRepository.save(pedido);

        // Keep the checkout response immediate. The simulated delivery flow advances in
        // the background with pauses so the client can still cancel while the order is paid.
        entregaService.simularPreparacaoRestauranteEAtribuirEntregadorComPausas(pedidoId);
    }
}
