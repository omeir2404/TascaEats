package pt.ul.fc.css.tascaeats.service;

import java.util.Comparator;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pt.ul.fc.css.tascaeats.domain.Driver;
import pt.ul.fc.css.tascaeats.domain.Entrega;
import pt.ul.fc.css.tascaeats.domain.EstadoPedido;
import pt.ul.fc.css.tascaeats.domain.Pedido;
import pt.ul.fc.css.tascaeats.dto.AtribuirEntregadorDTO;
import pt.ul.fc.css.tascaeats.repository.IEntregaRepository;
import pt.ul.fc.css.tascaeats.repository.IPedidoRepository;
import pt.ul.fc.css.tascaeats.repository.IUtilizadorRepository;

@Service
public class EntregaService {

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
    public Entrega atribuirEntregador(AtribuirEntregadorDTO atribuirEntregaDTO) { // ve se nao é necessario usar o entregadorID do DTO
        Long pedidoId = atribuirEntregaDTO.getPedidoId();
        if (pedidoId == null) {
            throw new IllegalArgumentException("ID do pedido inválido.");
        }

        Optional<Pedido> pedidoOptional = pedidoRepository.findById(pedidoId);
        if (pedidoOptional.isEmpty()) {
            throw new IllegalArgumentException("Pedido não encontrado: " + pedidoId);
        }
        Pedido pedido = pedidoOptional.get();
                Driver entregador = null;
                for (var utilizador : utilizadorRepository.findAll()) {
                    if (utilizador instanceof Driver) {
                    Driver candidato = (Driver) utilizador;
                    if (candidato.getAvailable() == true) {
                        if (entregador == null || candidato.getId() < entregador.getId()) {
                        entregador = candidato;
                        }
                    }
                    }
                }

                if (entregador == null) {
                    throw new IllegalStateException("Não há entregadores disponíveis.");
                }

        Entrega entrega = new Entrega(pedido, entregador.getId(), null);

        entregador.setAvailable(false);
        pedido.atribuirEntregador();

        utilizadorRepository.save(entregador);
        pedidoRepository.save(pedido);

        return entregaRepository.save(entrega);
    }
}