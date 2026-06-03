package pt.ul.fc.css.tascaeats.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pt.ul.fc.css.tascaeats.domain.menu.Menu;
import pt.ul.fc.css.tascaeats.domain.produto.Produto;
import pt.ul.fc.css.tascaeats.domain.restaurante.CatalogoEntregas;
import pt.ul.fc.css.tascaeats.domain.restaurante.Restaurante;
import pt.ul.fc.css.tascaeats.domain.utilizador.Morada;
import pt.ul.fc.css.tascaeats.domain.utilizador.RestauranteAdmin;
import pt.ul.fc.css.tascaeats.repository.menuRepository.IMenuRepository;
import pt.ul.fc.css.tascaeats.repository.produtoRepository.IProdutoRepository;
import pt.ul.fc.css.tascaeats.repository.restauranteRepository.IRestauranteRepository;
import pt.ul.fc.css.tascaeats.service.menuService.MenuService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock
    private IMenuRepository menuRepository;

    @Mock
    private IProdutoRepository produtoRepository;

    @Mock
    private IRestauranteRepository restauranteRepository;

    @InjectMocks
    private MenuService menuService;

    @Test
    void criarMenuValidaNomeEPersisteTrimado() {
        when(menuRepository.save(any(Menu.class))).thenAnswer(i -> i.getArgument(0));

        Menu menu = menuService.criarMenu("  Almoço  ");

        assertThat(menu.getNome()).isEqualTo("Almoço");
        verify(menuRepository).save(any(Menu.class));
    }

    @Test
    void criarMenuComNomeInvalidoFalha() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> menuService.criarMenu(" "));

        assertEquals("O nome do menu é obrigatório.", ex.getMessage());
    }

    @Test
    void atualizarMenuComNomeVazioFalha() {
        Menu menu = menuComId(1L, "Antigo");
        when(menuRepository.findById(1L)).thenReturn(Optional.of(menu));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> menuService.atualizarMenu(1L, " "));

        assertEquals("O nome do menu não pode ser vazio.", ex.getMessage());
    }

    @Test
    void atualizarMenuComNomeValidoPersiste() {
        Menu menu = menuComId(1L, "Antigo");
        when(menuRepository.findById(1L)).thenReturn(Optional.of(menu));
        when(menuRepository.save(menu)).thenReturn(menu);

        Menu atualizado = menuService.atualizarMenu(1L, "  Novo  ");

        assertThat(atualizado.getNome()).isEqualTo("Novo");
        verify(menuRepository).save(menu);
    }

    @Test
    void obterMenuComIdInvalidoOuInexistenteFalha() {
        IllegalArgumentException idInvalido = assertThrows(IllegalArgumentException.class,
                () -> menuService.obterMenuPorId(0L));
        assertEquals("O id do menu é obrigatório.", idInvalido.getMessage());

        when(menuRepository.findById(99L)).thenReturn(Optional.empty());
        IllegalArgumentException inexistente = assertThrows(IllegalArgumentException.class,
                () -> menuService.obterMenuPorId(99L));
        assertEquals("O menu não foi encontrado.", inexistente.getMessage());
    }

    @Test
    void associarProdutosAoMenuSubstituiProdutosEIgnoraIdsDuplicados() {
        Menu menu = menuComId(1L, "Menu");
        Produto antigo = produtoComId(10L, "Antigo");
        Produto novo = produtoComId(20L, "Novo");
        menu.adicionarProduto(antigo);

        when(menuRepository.findById(1L)).thenReturn(Optional.of(menu));
        when(produtoRepository.findAllById(List.of(20L))).thenReturn(List.of(novo));
        when(produtoRepository.saveAll(any())).thenAnswer(i -> i.getArgument(0));
        when(menuRepository.save(menu)).thenReturn(menu);

        Menu atualizado = menuService.associarProdutosAoMenu(1L, List.of(20L, 20L));

        assertThat(atualizado.getProdutos()).containsExactly(novo);
        assertThat(antigo.getMenu()).isNull();
        assertThat(novo.getMenu()).isSameAs(menu);
        verify(produtoRepository).saveAll(List.of(novo));
    }

    @Test
    void associarProdutosAoMenuComListaVaziaRemoveProdutosAtuais() {
        Menu menu = menuComId(1L, "Menu");
        Produto produto = produtoComId(10L, "Produto");
        menu.adicionarProduto(produto);

        when(menuRepository.findById(1L)).thenReturn(Optional.of(menu));
        when(produtoRepository.saveAll(any())).thenAnswer(i -> i.getArgument(0));
        when(menuRepository.save(menu)).thenReturn(menu);

        Menu atualizado = menuService.associarProdutosAoMenu(1L, List.of());

        assertThat(atualizado.getProdutos()).isEmpty();
        assertThat(produto.getMenu()).isNull();
    }

    @Test
    void associarProdutosAoMenuComProdutoInexistenteFalha() {
        Menu menu = menuComId(1L, "Menu");
        when(menuRepository.findById(1L)).thenReturn(Optional.of(menu));
        when(produtoRepository.findAllById(List.of(10L, 20L))).thenReturn(List.of(produtoComId(10L, "Um")));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> menuService.associarProdutosAoMenu(1L, List.of(10L, 20L)));

        assertEquals("Alguns produtos não foram encontrados.", ex.getMessage());
    }

    @Test
    void associarProdutosAoMenuComIdProdutoInvalidoFalha() {
        Menu menu = menuComId(1L, "Menu");
        when(menuRepository.findById(1L)).thenReturn(Optional.of(menu));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> menuService.associarProdutosAoMenu(1L, Arrays.asList(1L, null)));

        assertEquals("Há produtos com id inválido.", ex.getMessage());
    }

    @Test
    void associarMenuAoRestauranteValidaIdsEGravaMenu() {
        Restaurante restaurante = restauranteComId(5L);
        Menu menu = menuComId(9L, "Menu");

        when(restauranteRepository.findById(5L)).thenReturn(Optional.of(restaurante));
        when(menuRepository.findById(9L)).thenReturn(Optional.of(menu));
        when(restauranteRepository.save(restaurante)).thenReturn(restaurante);

        Restaurante atualizado = menuService.associarMenuAoRestaurante(5L, 9L);

        assertThat(atualizado.getMenu()).isSameAs(menu);
        verify(restauranteRepository).save(restaurante);
    }

    @Test
    void removerMenuDesassociaRestaurantesApagaProdutosEDeleteMenu() {
        Menu menu = menuComId(9L, "Menu");
        Produto ativo = produtoComId(1L, "Ativo");
        Produto apagado = produtoComId(2L, "Apagado");
        apagado.apagar();
        menu.adicionarProduto(ativo);
        menu.adicionarProduto(apagado);
        Restaurante restaurante = restauranteComId(5L);
        restaurante.setMenu(menu);

        when(menuRepository.findById(9L)).thenReturn(Optional.of(menu));
        when(restauranteRepository.findByMenuId(9L)).thenReturn(List.of(restaurante));
        when(restauranteRepository.saveAll(any())).thenAnswer(i -> i.getArgument(0));
        when(produtoRepository.saveAll(any())).thenAnswer(i -> i.getArgument(0));

        menuService.removerMenu(9L);

        assertThat(restaurante.getMenu()).isNull();
        assertThat(menu.getProdutos()).isEmpty();
        assertThat(ativo.isApagado()).isTrue();
        assertThat(apagado.isApagado()).isTrue();
        verify(menuRepository).delete(menu);
    }

    @Test
    void listarEProcurarMenusDelegamNoRepositorio() {
        Menu menu = menuComId(1L, "Menu");
        when(menuRepository.findAll()).thenReturn(List.of(menu));
        when(menuRepository.findByNomeContainingIgnoreCase("tasca")).thenReturn(List.of(menu));

        assertThat(menuService.listarMenus()).containsExactly(menu);
        assertThat(menuService.procurarMenuPorNome(" tasca ")).containsExactly(menu);
        assertThat(menuService.procurarMenuPorNome(" ")).containsExactly(menu);
    }

    private Menu menuComId(Long id, String nome) {
        Menu menu = new Menu(nome);
        ReflectionTestUtils.setField(menu, "id", id);
        return menu;
    }

    private Produto produtoComId(Long id, String nome) {
        Produto produto = new Produto(nome, 10.0, "Descrição", null);
        ReflectionTestUtils.setField(produto, "id", id);
        return produto;
    }

    private Restaurante restauranteComId(Long id) {
        Morada morada = new Morada();
        morada.setRua("Rua");
        morada.setNumero(1);
        morada.setCodigoPostal("1000-000");
        morada.setCidade("Lisboa");
        morada.setDistrito("Lisboa");
        morada.setPais("Portugal");
        Restaurante restaurante = new Restaurante(
                "Restaurante",
                "123456789",
                morada,
                new RestauranteAdmin("Admin", "admin@test.pt", "pw"),
                new CatalogoEntregas());
        ReflectionTestUtils.setField(restaurante, "id", id);
        return restaurante;
    }
}
