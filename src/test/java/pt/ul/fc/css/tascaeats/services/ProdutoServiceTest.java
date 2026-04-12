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
import pt.ul.fc.css.tascaeats.domain.produto.Produto;
import pt.ul.fc.css.tascaeats.domain.restaurante.CatalogoEntregas;
import pt.ul.fc.css.tascaeats.domain.restaurante.Restaurante;
import pt.ul.fc.css.tascaeats.domain.utilizador.Morada;
import pt.ul.fc.css.tascaeats.domain.utilizador.RestauranteAdmin;
import pt.ul.fc.css.tascaeats.dto.produtoDTO.AtualizarDisponibilidadeProdutoDTO;
import pt.ul.fc.css.tascaeats.dto.produtoDTO.AtualizarProdutoDTO;
import pt.ul.fc.css.tascaeats.dto.produtoDTO.CriarProdutoDTO;
import pt.ul.fc.css.tascaeats.repository.produtoRepository.IProdutoRepository;
import pt.ul.fc.css.tascaeats.repository.restauranteRepository.IRestauranteRepository;
import pt.ul.fc.css.tascaeats.service.produtoService.ProdutoService;

import java.util.List;
import java.util.Optional;

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
    @InjectMocks
    private ProdutoService produtoService;

    //A) Criar produto com DTO nulo.
    @Test
    @DisplayName("A: Criar produto com DTO nulo deve falhar.")
    void criarProdutoWithNullDTO() {
        try {
            produtoService.criarProduto(1L, null);
            fail("O DTO não pode ser nulo.");
        } catch (IllegalArgumentException e) {
            assertEquals("Os dados do produto são obrigatórios.", e.getMessage());
        }
    }


    //B) Criar produto com catálogo inexistente.
    @Test
    @DisplayName("B: Criar produto com restaurante inexistente.")
    void criarProdutoWithRestauranteInexistente() {
        CriarProdutoDTO dto = criarProdutoDTOValido();
        when(restauranteRepository.findById(1L)).thenReturn(Optional.empty());

        try {
            produtoService.criarProduto(1L, dto);
            fail("O restaurante não foi encontrado.");
        } catch (IllegalArgumentException e) {
            assertEquals("O restaurante não foi encontrado.", e.getMessage());
        }
    }


    //C) Criar produto com sucesso deve criar produto com `disponivel=true` e `apagado=false`.
    @Test
    @DisplayName("C: Criar produto com sucesso.")
    void criarProdutoSuccess() {
        CriarProdutoDTO dto = criarProdutoDTOValido();
        Restaurante restaurante = restauranteValido();

        when(restauranteRepository.findById(1L)).thenReturn(Optional.of(restaurante));
        when(produtoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Produto produto = produtoService.criarProduto(1L, dto);

        assertEquals("Massa", produto.getNome());
        assertEquals(10.0, produto.getPreco());
        assertEquals("Massa deliciosa", produto.getDescricao());

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

    private Restaurante restauranteValido() {
        Morada morada = new Morada();
        morada.setRua("Rua Teste");
        morada.setNumero(1);
        morada.setCodigoPostal("1000-100");
        morada.setCidade("Lisboa");
        morada.setDistrito("Lisboa");
        morada.setPais("Portugal");

        return new Restaurante(
                "Restaurante",
                "123456789",
                morada,
                new RestauranteAdmin("Admin", "admin@test.pt", "pw"),
                new CatalogoEntregas()
        );
    }

}
