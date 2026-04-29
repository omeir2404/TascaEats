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


}