// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.service.produtoService;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pt.ul.fc.css.tascaeats.domain.produto.Produto;
import pt.ul.fc.css.tascaeats.domain.restaurante.Restaurante;
import pt.ul.fc.css.tascaeats.dto.produtoDTO.AtualizarDisponibilidadeProdutoDTO;
import pt.ul.fc.css.tascaeats.dto.produtoDTO.AtualizarProdutoDTO;
import pt.ul.fc.css.tascaeats.dto.produtoDTO.CriarProdutoDTO;
import pt.ul.fc.css.tascaeats.repository.produtoRepository.IProdutoRepository;
import pt.ul.fc.css.tascaeats.repository.restauranteRepository.IRestauranteRepository;

@Service
public class ProdutoService {
    private final IProdutoRepository produtoRepository;
    private final IRestauranteRepository restauranteRepository;

    public ProdutoService(IProdutoRepository produtoRepository, IRestauranteRepository restauranteRepository) {
        this.produtoRepository = produtoRepository;
        this.restauranteRepository = restauranteRepository;
    }

    //Criar Produto
    public Produto criarProduto(Long restauranteId, CriarProdutoDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Os dados do produto são obrigatórios.");
        }

        Restaurante restaurante = restauranteRepository.findById(restauranteId).orElse(null);
        if (restaurante == null) {
            throw new IllegalArgumentException("O restaurante não foi encontrado.");
        }

        Produto produto = new Produto(dto.getNome(), dto.getPreco(), dto.getDescricao(), restaurante);
        return produtoRepository.save(produto);
    }

    //Atualizar disponibilidade
    public Produto atualizarDisponibilidade(Long id, AtualizarDisponibilidadeProdutoDTO dto) {
        if (dto == null || dto.getDisponivel() == null) {
            throw new IllegalArgumentException("A disponibilidade é obrigatória.");
        }

        Produto produto = obterProdutoPorId(id);
        if (produto.isApagado()) {
            throw new IllegalStateException("Não é possível alterar a disponibilidade de um produto apagado.");
        }

        if (dto.getDisponivel()) {
            produto.disponibilizar();
        } else {
            produto.indisponibilizar();
        }

        return produtoRepository.save(produto);
    }

    //Atualizar produto
    public Produto atualizarProduto(Long id, AtualizarProdutoDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Os dados para atualizar o produto são obrigatórios.");
        }

        Produto produto = obterProdutoPorId(id);
        if (produto.isApagado()) {
            throw new IllegalStateException("Não é possível atualizar um produto apagado.");
        }

        produto.setNome(dto.getNome());
        produto.setPreco(dto.getPreco());
        produto.setDescricao(dto.getDescricao());

        return produtoRepository.save(produto);
    }

    //Listar produtos
    public List<Produto> listarProdutosPorRestaurante(Long restauranteId) {
        if (restauranteId == null || restauranteId <= 0) {
            throw new IllegalArgumentException("O id do restaurante é obrigatório.");
        }
        return produtoRepository.findByRestauranteId(restauranteId);
    }

    public List<Produto> listarProdutos() {
        return produtoRepository.findAll();
    }

    public List<Produto> listarProdutosAtivos() {
        return produtoRepository.findByApagadoFalse();
    }

    public List<Produto> listarProdutosDisponiveis() {
        return produtoRepository.findByDisponivelTrue();
    }

    public List<Produto> procurarProdutoPorNome(String nome) {
        return produtoRepository.findByNomeContainingIgnoreCase(nome);
    }

    //Obter produto por id
    public Produto obterProdutoPorId(Long id) {
        Produto produto = produtoRepository.findById(id).orElse(null);
        if (produto == null) {
            throw new IllegalArgumentException("O produto não foi encontrado.");
        }
        return produto;
    }

    //Apagar produto (soft delete)
    public void apagarProduto(Long id) {
        Produto produto = obterProdutoPorId(id);
        produto.apagar();
        produtoRepository.save(produto);
    }

    // Pesquisar produtos com múltiplos filtros
    @Transactional(readOnly = true)
    public List<Produto> pesquisarProdutos(String nome,
                                            Double precoMaximo,
                                            Long menuId,
                                            Boolean disponivel,
                                            LocalDate dataInicioPopularidade,
                                            LocalDate dataFimPopularidade) {
        String nomeNormalizado = normalizarTexto(nome);

        List<Produto> candidatos = obterProdutosCandidatos(nomeNormalizado, menuId, disponivel);

        return candidatos.stream()
                .filter(produto -> precoMaximo == null || produto.getPreco() <= precoMaximo)
                .filter(produto -> !produto.isApagado())
                .filter(produto -> filtrarPopularidade(produto, dataInicioPopularidade, dataFimPopularidade))
                .toList();
    }

    private List<Produto> obterProdutosCandidatos(String nomeNormalizado, Long menuId, Boolean disponivel) {
        if (nomeNormalizado != null) {
            return produtoRepository.findByNomeContainingIgnoreCase(nomeNormalizado);
        } else if (menuId != null) {
            return produtoRepository.findByMenuId(menuId);
        } else if (disponivel != null && disponivel) {
            return produtoRepository.findByDisponivelTrue();
        } else {
            return produtoRepository.findByApagadoFalse();
        }
    }

    private boolean filtrarPopularidade(Produto produto, LocalDate dataInicio, LocalDate dataFim) {
        if (dataInicio == null || dataFim == null) {
            return true;
        }
        return produto.getPedidos() != null && !produto.getPedidos().isEmpty() &&
               produto.getPedidos().stream()
                    .anyMatch(pedido -> pedido.getData() != null && 
                           !pedido.getData().isBefore(dataInicio) && 
                           !pedido.getData().isAfter(dataFim));
    }

    private String normalizarTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        return texto.trim().toLowerCase(Locale.ROOT);
    }
}