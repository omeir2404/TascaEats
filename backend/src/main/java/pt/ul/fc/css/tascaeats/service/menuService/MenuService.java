// ##############################################################
// 					  TASCAEATS								#
// 					CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.service.menuService;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pt.ul.fc.css.tascaeats.domain.menu.Menu;
import pt.ul.fc.css.tascaeats.domain.produto.Produto;
import pt.ul.fc.css.tascaeats.domain.restaurante.Restaurante;
import pt.ul.fc.css.tascaeats.repository.menuRepository.IMenuRepository;
import pt.ul.fc.css.tascaeats.repository.produtoRepository.IProdutoRepository;
import pt.ul.fc.css.tascaeats.repository.restauranteRepository.IRestauranteRepository;

@Service
@Transactional
public class MenuService {

    private final IMenuRepository menuRepository;
    private final IProdutoRepository produtoRepository;
    private final IRestauranteRepository restauranteRepository;

    public MenuService(IMenuRepository menuRepository, IProdutoRepository produtoRepository, IRestauranteRepository restauranteRepository) {
        this.menuRepository = menuRepository;
        this.produtoRepository = produtoRepository;
        this.restauranteRepository = restauranteRepository;
    }

    public Menu criarMenu(String nome) {
        validarNome(nome);
        return menuRepository.save(new Menu(nome.trim()));
    }

    public Menu criarMenu(String nome, List<Long> produtosIds) {
        Menu menu = criarMenu(nome);
        if (produtosIds != null && !produtosIds.isEmpty()) {
            associarProdutosAoMenu(menu.getId(), produtosIds);
            return obterMenuPorId(menu.getId());
        }
        return menu;
    }

    public Menu atualizarMenu(Long menuId, String nome) {
        Menu menu = obterMenuPorId(menuId);
        validarNomeOpcional(nome);
        if (nome != null && !nome.isBlank()) {
            menu.setNome(nome.trim());
        }
        return menuRepository.save(menu);
    }

    public Menu atualizarMenu(Long menuId, String nome, List<Long> produtosIds) {
        Menu menu = atualizarMenu(menuId, nome);
        if (produtosIds != null && !produtosIds.isEmpty()) {
            associarProdutosAoMenu(menu.getId(), produtosIds);
            return obterMenuPorId(menu.getId());
        }
        return menu;
    }

    public Menu associarProdutosAoMenu(Long menuId, List<Long> produtosIds) {
        Menu menu = obterMenuPorId(menuId);
        List<Long> idsNormalizados = normalizarIds(produtosIds);
        if (idsNormalizados.isEmpty()) {
            return menu;
        }

        List<Produto> produtos = produtoRepository.findAllById(idsNormalizados);
        if (produtos.size() != idsNormalizados.size()) {
            throw new IllegalArgumentException("Alguns produtos não foram encontrados.");
        }

        Set<Long> existentes = new LinkedHashSet<>();
        for (Produto produto : menu.getProdutos()) {
            if (produto.getId() != null) {
                existentes.add(produto.getId());
            }
        }

        for (Produto produto : produtos) {
            produto.setMenu(menu);
            if (produto.getId() != null && !existentes.contains(produto.getId())) {
                menu.adicionarProduto(produto);
                existentes.add(produto.getId());
            }
        }

        produtoRepository.saveAll(produtos);
        return menuRepository.save(menu);
    }

    public Restaurante associarMenuAoRestaurante(Long restauranteId, Long menuId) {
        if (restauranteId == null || restauranteId <= 0) {
            throw new IllegalArgumentException("O id do restaurante é obrigatório.");
        }
        Restaurante restaurante = restauranteRepository.findById(restauranteId)
                .orElseThrow(() -> new IllegalArgumentException("O restaurante não foi encontrado."));
        Menu menu = obterMenuPorId(menuId);

        restaurante.setMenu(menu);
        return restauranteRepository.save(restaurante);
    }

    @Transactional(readOnly = true)
    public List<Menu> listarMenus() {
        return menuRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Menu> procurarMenuPorNome(String nome) {
        if (nome == null || nome.isBlank()) {
            return menuRepository.findAll();
        }
        return menuRepository.findByNomeContainingIgnoreCase(nome.trim());
    }

    @Transactional(readOnly = true)
    public Menu obterMenuPorId(Long menuId) {
        if (menuId == null || menuId <= 0) {
            throw new IllegalArgumentException("O id do menu é obrigatório.");
        }
        Optional<Menu> menu = menuRepository.findById(menuId);
        if (menu.isEmpty()) {
            throw new IllegalArgumentException("O menu não foi encontrado.");
        }
        return menu.get();
    }

    private void validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome do menu é obrigatório.");
        }
    }

    private void validarNomeOpcional(String nome) {
        if (nome != null && nome.isBlank()) {
            throw new IllegalArgumentException("O nome do menu não pode ser vazio.");
        }
    }

    private List<Long> normalizarIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        Set<Long> unicos = new LinkedHashSet<>();
        for (Long id : ids) {
            if (id == null || id <= 0) {
                throw new IllegalArgumentException("Há produtos com id inválido.");
            }
            unicos.add(id);
        }
        return new ArrayList<>(unicos);
    }

}