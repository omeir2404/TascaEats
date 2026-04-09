package pt.ul.fc.css.tascaeats.service.produtoService;

import java.util.List;

import org.springframework.stereotype.Service;

import pt.ul.fc.css.tascaeats.domain.produto.CatalogoProdutos;
import pt.ul.fc.css.tascaeats.domain.produto.Produto;
import pt.ul.fc.css.tascaeats.dto.produtoDTO.AtualizarDisponibilidadeProdutoDTO;
import pt.ul.fc.css.tascaeats.dto.produtoDTO.AtualizarProdutoDTO;
import pt.ul.fc.css.tascaeats.dto.produtoDTO.CriarProdutoDTO;
import pt.ul.fc.css.tascaeats.repository.produtoRepository.ICatalogoProdutosRepository;
import pt.ul.fc.css.tascaeats.repository.produtoRepository.IProdutoRepository;

@Service
public class ProdutoService {
    private final IProdutoRepository produtoRepository;
    private final ICatalogoProdutosRepository catalogoProdutosRepository;

    public ProdutoService(IProdutoRepository produtoRepository, ICatalogoProdutosRepository catalogoProdutosRepository) {
        this.produtoRepository = produtoRepository;
        this.catalogoProdutosRepository = catalogoProdutosRepository;
    }

    //Criar Produto
    public Produto criarProduto(Long catalogoId, CriarProdutoDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Os dados do produto são obrigatórios.");
        }

        CatalogoProdutos catalogo = catalogoProdutosRepository.findById(catalogoId).orElse(null);
        if (catalogo == null) {
            throw new IllegalArgumentException("O catálogo não foi encontrado.");
        }

        Produto produto = new Produto(dto.getNome(), dto.getPreco(), dto.getDescricao(), catalogo);
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
        return produtoRepository.findByCatalogoProdutosRestauranteId(restauranteId);
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
}