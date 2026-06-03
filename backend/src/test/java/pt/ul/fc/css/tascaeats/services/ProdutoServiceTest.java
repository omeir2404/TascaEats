// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pt.ul.fc.css.tascaeats.domain.menu.Menu;
import pt.ul.fc.css.tascaeats.domain.pedido.Pedido;
import pt.ul.fc.css.tascaeats.domain.produto.Produto;
import pt.ul.fc.css.tascaeats.domain.restaurante.CatalogoEntregas;
import pt.ul.fc.css.tascaeats.domain.restaurante.Restaurante;
import pt.ul.fc.css.tascaeats.domain.utilizador.Morada;
import pt.ul.fc.css.tascaeats.domain.utilizador.RestauranteAdmin;
import pt.ul.fc.css.tascaeats.dto.produtoDTO.AtualizarDisponibilidadeProdutoDTO;
import pt.ul.fc.css.tascaeats.dto.produtoDTO.AtualizarProdutoDTO;
import pt.ul.fc.css.tascaeats.dto.produtoDTO.CriarProdutoDTO;
import pt.ul.fc.css.tascaeats.repository.menuRepository.IMenuRepository;
import pt.ul.fc.css.tascaeats.repository.produtoRepository.IProdutoRepository;
import pt.ul.fc.css.tascaeats.repository.restauranteRepository.IRestauranteRepository;
import pt.ul.fc.css.tascaeats.service.produtoService.ProdutoService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock
    private IProdutoRepository produtoRepository;
    @Mock
    private IRestauranteRepository restauranteRepository;
    @Mock
    private IMenuRepository menuRepository;
    @InjectMocks
    private ProdutoService produtoService;

    //A) Criar produto com DTO nulo.
    @Test
    @DisplayName("A: Criar produto com DTO nulo deve falhar.")
    void criarProdutoWithNullDTO() {
        try {
            produtoService.criarProdutoNoMenu(1L, null);
            fail("O DTO não pode ser nulo.");
        } catch (IllegalArgumentException e) {
            assertEquals("Os dados do produto são obrigatórios.", e.getMessage());
        }
    }


    //B) Criar produto com menu inexistente.
    @Test
    @DisplayName("B: Criar produto com menu inexistente.")
    void criarProdutoWithMenuInexistente() {
        CriarProdutoDTO dto = criarProdutoDTOValido();
        when(menuRepository.findById(1L)).thenReturn(Optional.empty());

        try {
            produtoService.criarProdutoNoMenu(1L, dto);
            fail("O menu não foi encontrado.");
        } catch (IllegalArgumentException e) {
            assertEquals("O menu não foi encontrado.", e.getMessage());
        }
    }


    //C) Criar produto com sucesso deve criar produto com `disponivel=true` e `apagado=false`.
    @Test
    @DisplayName("C: Criar produto com sucesso.")
    void criarProdutoSuccess() {
        CriarProdutoDTO dto = criarProdutoDTOValido();
        Menu menu = new Menu("Menu Teste");

        when(menuRepository.findById(1L)).thenReturn(Optional.of(menu));
        when(produtoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Produto produto = produtoService.criarProdutoNoMenu(1L, dto);

        assertEquals("Massa", produto.getNome());
        assertEquals(10.0, produto.getPreco());
        assertEquals("Massa deliciosa", produto.getDescricao());
        assertSame(menu, produto.getMenu());

        assertTrue(produto.isDisponivel());
        assertFalse(produto.isApagado());
    }


    //D0) Listar todos os produtos deve devolver o que vem do repositório.
    @Test
    @DisplayName("D0: Listar todos os produtos deve devolver a lista do repositório.")
    void listarProdutosDeveDevolverTodosOsProdutos() {
        Produto produto1 = produtoValido();
        Produto produto2 = new Produto("Bolo", 4.5, "Sobremesa", restauranteValido());
        when(produtoRepository.findAll()).thenReturn(List.of(produto1, produto2));

        List<Produto> result = produtoService.listarProdutos();

        assertEquals(2, result.size());
        assertEquals("Massa", result.get(0).getNome());
        assertEquals("Bolo", result.get(1).getNome());
        verify(produtoRepository).findAll();
    }


    //D) Atualizar produto com DTO nulo.
    @Test
    @DisplayName("D: Atualizar produto com DTO nulo.")
    void atualizarProdutoWithNullDTO() {
        try {
            produtoService.atualizarProduto(1L, null);
            fail("DTO obrigatório.");
        } catch (IllegalArgumentException e) {
            assertEquals("Os dados para atualizar o produto são obrigatórios.", e.getMessage());
        }
    }


    //E) Atualizar produto com produto inexistente.
    @Test
    @DisplayName("E: Atualizar produto inexistente deve falhar.")
    void atualizarProdutoInexistente() {
        when(produtoRepository.findById(99L)).thenReturn(Optional.empty());

        try {
            produtoService.atualizarProduto(99L, atualizarProdutoDTOValido());
            fail("Produto inexistente.");
        } catch (IllegalArgumentException e) {
            assertEquals("O produto não foi encontrado.", e.getMessage());
        }
    }


    //F) Atualizar produto em produto apagado.
    @Test
    @DisplayName("F: Atualizar produto apagado deve falhar.")
    void atualizarProdutoApagado() {
        Produto produto = produtoValido();
        produto.apagar();

        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));

        try {
            produtoService.atualizarProduto(1L, atualizarProdutoDTOValido());
            fail("Produto apagado.");
        } catch (IllegalStateException e) {
            assertEquals("Não é possível atualizar um produto apagado.", e.getMessage());
        }
    }


    //G) Atualizar produto com sucesso deve persistir `nome`, `preco` e `descricao`.
    @Test
    @DisplayName("G: Atualizar produto com sucesso.")
    void atualizarProdutoSuccess() {
        Produto produto = produtoValido();

        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(produtoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Produto atualizado = produtoService.atualizarProduto(1L, atualizarProdutoDTOValido());

        assertEquals("Massa Atualizada", atualizado.getNome());
        assertEquals(12.0, atualizado.getPreco());
        assertEquals("Nova descrição", atualizado.getDescricao());
    }


    //H) Atualizar disponibilidade com DTO nulo ou estado `disponivel` nulo.
    @Test
    @DisplayName("H: Atualizar disponibilidade com DTO nulo ou disponibilidade nula.")
    void atualizarDisponibilidadeDTOInvalido() {

        // Caso 1: DTO nulo
        try {
            produtoService.atualizarDisponibilidade(1L, null);
            fail("DTO não pode ser nulo.");
        } catch (IllegalArgumentException e) {
            assertEquals("A disponibilidade é obrigatória.", e.getMessage());
        }

        // Caso 2: Disponibilidade nula
        try {
            AtualizarDisponibilidadeProdutoDTO dto = new AtualizarDisponibilidadeProdutoDTO();
            dto.setDisponivel(null);
            produtoService.atualizarDisponibilidade(1L, dto);
            fail("Disponibilidade não pode ser nula.");
        } catch (IllegalArgumentException e) {
            assertEquals("A disponibilidade é obrigatória.", e.getMessage());
        }
    }


    //I) Atualizar disponibilidade com produto inexistente.
    @Test
    @DisplayName("I: Atualizar disponibilidade com produto inexistente deve falhar.")
    void atualizarDisponibilidadeProdutoInexistente() {
        AtualizarDisponibilidadeProdutoDTO dto = new AtualizarDisponibilidadeProdutoDTO();
        dto.setDisponivel(false);

        when(produtoRepository.findById(99L)).thenReturn(Optional.empty());

        try {
            produtoService.atualizarDisponibilidade(99L, dto);
            fail("Produto inexistente.");
        } catch (IllegalArgumentException e) {
            assertEquals("O produto não foi encontrado.", e.getMessage());
        }
    }


    //J) Atualizar disponibilidade em produto apagado.
    @Test
    @DisplayName("J: Atualizar disponibilidade em produto apagado.")
    void atualizarDisponibilidadeProdutoApagado() {
        Produto produto = produtoValido();
        produto.apagar();

        AtualizarDisponibilidadeProdutoDTO dto = new AtualizarDisponibilidadeProdutoDTO();
        dto.setDisponivel(false);
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));

        try {
            produtoService.atualizarDisponibilidade(1L, dto);
            fail("Produto apagado.");
        } catch (IllegalStateException e) {
            assertEquals("Não é possível alterar a disponibilidade de um produto apagado.", e.getMessage());
        }
    }


    //K) Atualizar disponibilidade para false deve indisponibilizar produto ativo.
    @Test
    @DisplayName("K: Atualizar disponibilidade para false deve indisponibilizar produto ativo.")
    void atualizarDisponibilidadeParaFalse() {
        Produto produto = produtoValido();

        AtualizarDisponibilidadeProdutoDTO dto = new AtualizarDisponibilidadeProdutoDTO();
        dto.setDisponivel(false);

        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(produtoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Produto atualizado = produtoService.atualizarDisponibilidade(1L, dto);

        assertFalse(atualizado.isDisponivel());
    }


    //L) Atualizar disponibilidade para true deve disponibilizar produto previamente indisponível.
    @Test
    @DisplayName("L: Atualizar disponibilidade para true deve disponibilizar produto previamente indisponível.")
    void atualizarDisponibilidadeParaTrue() {
        Produto produto = produtoValido();
        produto.indisponibilizar();

        AtualizarDisponibilidadeProdutoDTO dto = new AtualizarDisponibilidadeProdutoDTO();
        dto.setDisponivel(true);

        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(produtoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Produto atualizado = produtoService.atualizarDisponibilidade(1L, dto);

        assertTrue(atualizado.isDisponivel());
    }


    //M) Obter produto por id com id inexistente.
    @Test
    @DisplayName("M: Obter produto por id inexistente.")
    void obterProdutoPorIdInexistente() {
        when(produtoRepository.findById(999L)).thenReturn(Optional.empty());

        try {
            produtoService.obterProdutoPorId(999L);
            fail("Produto inexistente.");
        } catch (IllegalArgumentException e) {
            assertEquals("O produto não foi encontrado.", e.getMessage());
        }
    }


    //N) Listar produtos ativos deve excluir produtos apagados.
    @Test
    @DisplayName("N: Listar produtos ativos deve excluir produtos apagados.")
    void listarProdutosAtivosExcluiApagados() {
        Produto ativo = produtoValido();
        when(produtoRepository.findByApagadoFalse()).thenReturn(List.of(ativo));

        List<Produto> result = produtoService.listarProdutosAtivos();

        assertEquals(1, result.size());
        assertFalse(result.get(0).isApagado());
        verify(produtoRepository).findByApagadoFalse();
    }


    //O) Listar produtos disponíveis deve devolver apenas disponivel=true.
    @Test
    @DisplayName("O: Listar produtos disponíveis deve devolver apenas disponivel=true.")
    void listarProdutosDisponiveisApenasTrue() {
        Produto disponivel = produtoValido();
        when(produtoRepository.findByDisponivelTrue()).thenReturn(List.of(disponivel));

        List<Produto> result = produtoService.listarProdutosDisponiveis();

        assertEquals(1, result.size());
        assertTrue(result.get(0).isDisponivel());
        verify(produtoRepository).findByDisponivelTrue();
    }


    //P) Procurar produto por nome deve usar pesquisa case-insensitive.
    @Test
    @DisplayName("P: Procurar produto por nome deve usar pesquisa case-insensitive.")
    void procurarProdutoPorNomeCaseInsensitive() {
        Produto massa = produtoValido();
        when(produtoRepository.findByNomeContainingIgnoreCase("mAsSa")).thenReturn(List.of(massa));

        List<Produto> result = produtoService.procurarProdutoPorNome("mAsSa");

        assertEquals(1, result.size());
        assertEquals("Massa", result.get(0).getNome());
        verify(produtoRepository).findByNomeContainingIgnoreCase("mAsSa");
    }


    //Q) Apagar produto com id inexistente.
    @Test
    @DisplayName("Q: Apagar produto com id inexistente.")
    void apagarProdutoInexistente() {
        when(produtoRepository.findById(404L)).thenReturn(Optional.empty());

        try {
            produtoService.apagarProduto(404L);
            fail("Produto inexistente.");
        } catch (IllegalArgumentException e) {
            assertEquals("O produto não foi encontrado.", e.getMessage());
        }
    }


    //R) Apagar produto com sucesso deve marcar apagado=true e disponivel=false.
    @Test
    @DisplayName("R: Apagar produto com sucesso deve marcar apagado=true e disponivel=false.")
    void apagarProdutoSuccess() {
        Produto produto = produtoValido();
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(produtoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        produtoService.apagarProduto(1L);

        assertTrue(produto.isApagado());
        assertFalse(produto.isDisponivel());
        verify(produtoRepository).save(produto);
    }

    @Test
    @DisplayName("S: Criar produto pelo caminho antigo deve falhar.")
    void criarProdutoLegacyFalha() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> produtoService.criarProduto(1L, criarProdutoDTOValido()));

        assertEquals("Produtos devem ser criados diretamente num menu.", ex.getMessage());
    }

    @Test
    @DisplayName("T: Listar produtos por restaurante junta produtos do menu e legados sem duplicar.")
    void listarProdutosPorRestauranteCombinaMenuELegados() {
        Menu menu = menuComId(7L);
        Restaurante restaurante = restauranteValido();
        restaurante.setMenu(menu);
        Produto produtoMenu = produtoComId(1L, "Menu");
        Produto produtoLegado = produtoComId(2L, "Legado");

        when(restauranteRepository.findById(10L)).thenReturn(Optional.of(restaurante));
        when(produtoRepository.findByMenuId(7L)).thenReturn(List.of(produtoMenu));
        when(produtoRepository.findByRestauranteId(10L)).thenReturn(List.of(produtoMenu, produtoLegado));

        List<Produto> result = produtoService.listarProdutosPorRestaurante(10L);

        assertThat(result).containsExactly(produtoMenu, produtoLegado);
    }

    @Test
    @DisplayName("U: Listar produtos por restaurante valida id e existência.")
    void listarProdutosPorRestauranteValidaIdEExistencia() {
        assertEquals("O id do restaurante é obrigatório.",
                assertThrows(IllegalArgumentException.class,
                        () -> produtoService.listarProdutosPorRestaurante(0L)).getMessage());

        when(restauranteRepository.findById(99L)).thenReturn(Optional.empty());
        assertEquals("O restaurante não foi encontrado.",
                assertThrows(IllegalArgumentException.class,
                        () -> produtoService.listarProdutosPorRestaurante(99L)).getMessage());
    }

    @Test
    @DisplayName("V: Listar produtos por menu valida id e delega no repositório.")
    void listarProdutosPorMenuValidaIdELista() {
        assertEquals("O id do menu é obrigatório.",
                assertThrows(IllegalArgumentException.class,
                        () -> produtoService.listarProdutosPorMenu(null)).getMessage());

        Produto produto = produtoValido();
        when(produtoRepository.findByMenuId(7L)).thenReturn(List.of(produto));

        assertThat(produtoService.listarProdutosPorMenu(7L)).containsExactly(produto);
    }

    @Test
    @DisplayName("W: Pesquisar produtos aplica filtros de nome, preço, apagado e popularidade.")
    void pesquisarProdutosFiltraPorNomePrecoAtivoEPopularidade() {
        Produto baratoPopular = produtoComId(1L, "Massa");
        baratoPopular.setPedidos(List.of(pedidoEm(LocalDate.of(2026, 6, 1))));
        Produto caro = produtoComId(2L, "Massa cara");
        caro.setPreco(99.0);
        Produto apagado = produtoComId(3L, "Massa apagada");
        apagado.apagar();

        when(produtoRepository.findByNomeContainingIgnoreCase("massa"))
                .thenReturn(List.of(baratoPopular, caro, apagado));

        List<Produto> result = produtoService.pesquisarProdutos(
                "  MASSA  ",
                20.0,
                null,
                null,
                LocalDate.of(2026, 5, 30),
                LocalDate.of(2026, 6, 3));

        assertThat(result).containsExactly(baratoPopular);
    }

    @Test
    @DisplayName("X: Pesquisar produtos escolhe candidatos por menu, disponibilidade ou ativos.")
    void pesquisarProdutosEscolheFonteDeCandidatos() {
        Produto porMenu = produtoValido();
        Produto disponivel = produtoValido();
        Produto ativo = produtoValido();

        when(produtoRepository.findByMenuId(7L)).thenReturn(List.of(porMenu));
        when(produtoRepository.findByDisponivelTrue()).thenReturn(List.of(disponivel));
        when(produtoRepository.findByApagadoFalse()).thenReturn(List.of(ativo));

        assertThat(produtoService.pesquisarProdutos(null, null, 7L, null, null, null)).containsExactly(porMenu);
        assertThat(produtoService.pesquisarProdutos(null, null, null, true, null, null)).containsExactly(disponivel);
        assertThat(produtoService.pesquisarProdutos(null, null, null, false, null, null)).containsExactly(ativo);
    }


    //**********Funções auxiliares************

    private CriarProdutoDTO criarProdutoDTOValido() {
        CriarProdutoDTO dto = new CriarProdutoDTO();
        dto.setNome("Massa");
        dto.setPreco(10.0);
        dto.setDescricao("Massa deliciosa");
        return dto;
    }

    private AtualizarProdutoDTO atualizarProdutoDTOValido() {
        AtualizarProdutoDTO dto = new AtualizarProdutoDTO();
        dto.setNome("Massa Atualizada");
        dto.setPreco(12.0);
        dto.setDescricao("Nova descrição");
        return dto;
    }

    private Produto produtoValido() {
        return new Produto("Massa", 10.0, "Descrição", restauranteValido());
    }

    private Produto produtoComId(Long id, String nome) {
        Produto produto = new Produto(nome, 10.0, "Descrição", restauranteValido());
        ReflectionTestUtils.setField(produto, "id", id);
        return produto;
    }

    private Menu menuComId(Long id) {
        Menu menu = new Menu("Menu");
        ReflectionTestUtils.setField(menu, "id", id);
        return menu;
    }

    private Pedido pedidoEm(LocalDate data) {
        Pedido pedido = new Pedido(1L, 1L, 10L);
        pedido.setData(data);
        return pedido;
    }

    private Restaurante restauranteValido() {
        Morada morada = new Morada();
        morada.setRua("Rua Teste");
        morada.setNumero(1);
        morada.setCodigoPostal("1000-100");
        morada.setCidade("Lisboa");
        morada.setDistrito("Lisboa");
        morada.setPais("Portugal");

        Restaurante restaurante = new Restaurante(
                "Restaurante",
                "123456789",
                morada,
                new RestauranteAdmin("Admin", "admin@test.pt", "pw"),
                new CatalogoEntregas()
        );
        ReflectionTestUtils.setField(restaurante, "id", 10L);
        return restaurante;
    }

}
