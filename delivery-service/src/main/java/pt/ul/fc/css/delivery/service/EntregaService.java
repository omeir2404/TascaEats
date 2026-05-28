package pt.ul.fc.css.delivery.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pt.ul.fc.css.delivery.domain.entrega.Entrega;
import pt.ul.fc.css.delivery.domain.entrega.EstadoEntrega;
import pt.ul.fc.css.delivery.domain.entregador.Entregador;
import pt.ul.fc.css.delivery.dto.entregaDTO.AtribuirEntregadorDTO;
import pt.ul.fc.css.delivery.dto.entregadorDTO.AtualizarEntregadorDTO;
import pt.ul.fc.css.delivery.dto.entregadorDTO.CriarEntregadorDTO;
import pt.ul.fc.css.delivery.repository.EntregaRepository;
import pt.ul.fc.css.delivery.repository.EntregadorRepository;

@Service
public class EntregaService {

    private static final String ENDERECO_ENTREGA_PENDENTE = "ENDERECO_NAO_INFORMADO";

    private final EntregaRepository entregaRepository;
    private final EntregadorRepository entregadorRepository;

    public EntregaService(EntregaRepository entregaRepository, EntregadorRepository entregadorRepository) {
        this.entregaRepository = entregaRepository;
        this.entregadorRepository = entregadorRepository;
    }

    @Transactional
    public Entregador registarEntregador(CriarEntregadorDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Entregador nao pode ser nulo.");
        }
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email do entregador e obrigatorio.");
        }
        Entregador entregador = new Entregador(dto.getNome(), dto.getEmail());
        return entregadorRepository.save(entregador);
    }

    @Transactional
    public Entregador atualizarEntregador(Long entregadorId, AtualizarEntregadorDTO dto) {
        if (entregadorId == null) {
            throw new IllegalArgumentException("ID do entregador invalido.");
        }
        Entregador entregador = entregadorRepository.findById(entregadorId)
                .orElseThrow(() -> new IllegalArgumentException("Entregador nao encontrado: " + entregadorId));
        if (dto == null) {
            throw new IllegalArgumentException("Novos dados do entregador nao podem ser nulos.");
        }

        if (dto.getNome() != null) {
            entregador.setNome(dto.getNome());
        }
        if (dto.getEmail() != null) {
            entregador.setEmail(dto.getEmail());
        }
        if (dto.getAvailable() != null) {
            entregador.setAvailable(dto.getAvailable());
        }
        return entregadorRepository.save(entregador);
    }

    @Transactional(readOnly = true)
    public List<Entregador> listarEntregadores() {
        return entregadorRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Entregador> listarEntregadoresDisponiveis() {
        return entregadorRepository.findByAvailable(true);
    }

    @Transactional(readOnly = true)
    public Optional<Entregador> obterEntregadorPorId(Long entregadorId) {
        if (entregadorId == null) {
            throw new IllegalArgumentException("ID do entregador invalido.");
        }
        return entregadorRepository.findById(entregadorId);
    }

    @Transactional(readOnly = true)
    public Optional<Entregador> obterEntregadorPorEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email do entregador invalido.");
        }
        return Optional.ofNullable(entregadorRepository.findByEmail(email));
    }

    @Transactional
    public Entrega atribuirEntregador(AtribuirEntregadorDTO dto) {
        if (dto == null || dto.getPedidoId() == null) {
            throw new IllegalArgumentException("Pedido Id nao pode ser nulo");
        }
        if (entregaRepository.findByPedidoId(dto.getPedidoId()).isPresent()) {
            throw new IllegalStateException("O pedido ja tem uma entrega atribuida.");
        }

        Entregador entregador = selecionarEntregadorDisponivel();
        Entrega entrega = new Entrega(dto.getPedidoId(), entregador.getId(), ENDERECO_ENTREGA_PENDENTE);

        entregador.setAvailable(false);
        entregadorRepository.save(entregador);
        return entregaRepository.save(entrega);
    }

    @Transactional(readOnly = true)
    public Optional<Entrega> obterEntregaPorPedido(Long pedidoId) {
        if (pedidoId == null) {
            throw new IllegalArgumentException("ID do pedido invalido.");
        }
        return entregaRepository.findByPedidoId(pedidoId);
    }

    @Transactional
    public Entrega registarRecolha(Long pedidoId) {
        Entrega entrega = obterEntregaObrigatoria(pedidoId);
        entrega.registarRecolha();
        return entregaRepository.save(entrega);
    }

    @Transactional
    public Entrega registarEntregaFinal(Long pedidoId) {
        Entrega entrega = obterEntregaObrigatoria(pedidoId);
        entrega.registarConclusao();

        Long entregadorId = entrega.getEntregadorId();
        if (entregadorId == null) {
            throw new IllegalArgumentException("Entregador nao encontrado: null");
        }

        Entregador entregador = entregadorRepository.findById(entregadorId)
                .orElseThrow(() -> new IllegalArgumentException("Entregador nao encontrado: " + entregadorId));
        entregador.setAvailable(true);

        entregadorRepository.save(entregador);
        return entregaRepository.save(entrega);
    }

    @Transactional(readOnly = true)
    public List<Entrega> listarEntregasEmCurso() {
        return entregaRepository.findByHoraEntregaIsNull();
    }

    private Entrega obterEntregaObrigatoria(Long pedidoId) {
        if (pedidoId == null) {
            throw new IllegalArgumentException("ID do pedido invalido.");
        }
        return entregaRepository.findByPedidoId(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException("Entrega nao encontrada para pedido: " + pedidoId));
    }

    private Entregador selecionarEntregadorDisponivel() {
        List<Entregador> entregadoresDisponiveis = entregadorRepository.findByAvailable(true);
        for (Entregador candidato : entregadoresDisponiveis) {
            if (!entregaRepository.existsByEntregadorIdAndHoraEntregaIsNull(candidato.getId())) {
                return candidato;
            }
        }
        throw new IllegalStateException("Nao ha entregadores disponiveis de momento.");
    }
}
