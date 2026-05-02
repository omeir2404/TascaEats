// ##############################################################
// 					TASCAEATS      			         			#
// 					CSS GRUPO: 25	     			    		#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.controllers;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import pt.ul.fc.css.tascaeats.domain.produto.Produto;
import pt.ul.fc.css.tascaeats.domain.restaurante.CatalogoEntregas;
import pt.ul.fc.css.tascaeats.domain.restaurante.Restaurante;
import pt.ul.fc.css.tascaeats.domain.utilizador.Morada;
import pt.ul.fc.css.tascaeats.domain.utilizador.RestauranteAdmin;
import pt.ul.fc.css.tascaeats.dto.produtoDTO.AtualizarDisponibilidadeProdutoDTO;
import pt.ul.fc.css.tascaeats.dto.produtoDTO.CriarProdutoDTO;
import pt.ul.fc.css.tascaeats.service.produtoService.ProdutoService;

@WebMvcTest(ProdutoController.class)
class ProdutoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProdutoService produtoService;

    @Test
        @DisplayName("A: POST /api/restaurantes/{restauranteId}/produtos com payload válido retorna 201.")
    void shouldCreateProdutoWhenPayloadIsValid() throws Exception {
        CriarProdutoDTO dto = criarProdutoDTO();
        Produto produto = produto(1L, "Massa", 10.0, "Massa fresca", true, false);

        when(produtoService.criarProduto(eq(4L), any(CriarProdutoDTO.class))).thenReturn(produto);

        mockMvc.perform(post("/api/restaurantes/4/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Produto criado com sucesso"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Massa"))
                .andExpect(jsonPath("$.preco").value(10.0))
                .andExpect(jsonPath("$.descricao").value("Massa fresca"));
    }

    @Test
    @DisplayName("B: POST /api/restaurantes/{restauranteId}/produtos com restaurante inexistente retorna 400.")
    void shouldReturnBadRequestWhenCatalogoDoesNotExist() throws Exception {
        when(produtoService.criarProduto(eq(99L), any(CriarProdutoDTO.class)))
                .thenThrow(new IllegalArgumentException("O restaurante não foi encontrado."));

        mockMvc.perform(post("/api/restaurantes/99/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criarProdutoDTO())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("O restaurante não foi encontrado."));
    }

    @Test
    @DisplayName("C: PUT /api/produtos/{id} com sucesso retorna 200.")
    void shouldUpdateProdutoWhenRequestIsValid() throws Exception {
        Produto produto = produto(2L, "Pizza", 12.5, "Pizza grande", true, false);

        when(produtoService.atualizarProduto(eq(2L), any())).thenReturn(produto);

        mockMvc.perform(put("/api/produtos/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(produtoUpdatePayload("Pizza", 12.5, "Pizza grande"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.nome").value("Pizza"))
                .andExpect(jsonPath("$.preco").value(12.5))
                .andExpect(jsonPath("$.disponivel").value(true))
                .andExpect(jsonPath("$.apagado").value(false));
    }

    @Test
    @DisplayName("D: PUT /api/produtos/{id} com produto inexistente retorna 404")
    void shouldReturnNotFoundWhenUpdatingMissingProduto() throws Exception {
        when(produtoService.atualizarProduto(eq(999L), any()))
                .thenThrow(new IllegalArgumentException("O produto não foi encontrado."));

        mockMvc.perform(put("/api/produtos/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(produtoUpdatePayload("Pizza", 12.5, "Pizza grande"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Produto n\u00e3o encontrado"));
    }

    @Test
    @DisplayName("E: PUT /api/produtos/{id} com produto apagado retorna 409.")
    void shouldReturnConflictWhenUpdatingDeletedProduto() throws Exception {
        when(produtoService.atualizarProduto(eq(5L), any()))
                .thenThrow(new IllegalStateException("Nao e possivel atualizar um produto apagado."));

        mockMvc.perform(put("/api/produtos/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(produtoUpdatePayload("Pizza", 12.5, "Pizza grande"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Nao e possivel atualizar um produto apagado."));
    }

    @Test
    @DisplayName("F: PATCH /api/produtos/{id}/disponibilidade com sucesso retorna 200")
    void shouldUpdateDisponibilidadeWhenRequestIsValid() throws Exception {
        AtualizarDisponibilidadeProdutoDTO dto = disponibilidadeDTO(false);
        Produto produto = produto(6L, "Hamburguer", 8.0, "Menu", false, false);

        when(produtoService.atualizarDisponibilidade(eq(6L), any(AtualizarDisponibilidadeProdutoDTO.class)))
                .thenReturn(produto);

        mockMvc.perform(patch("/api/produtos/6/disponibilidade")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(6))
                .andExpect(jsonPath("$.disponivel").value(false));
    }

    @Test
    @DisplayName("G: PATCH /api/produtos/{id}/disponibilidade com disponível ausente retorna 400.")
    void shouldReturnBadRequestWhenDisponivelIsMissing() throws Exception {
        mockMvc.perform(patch("/api/produtos/6/disponibilidade")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("H: PATCH /api/produtos/{id}/disponibilidade em produto apagado retorna 409.")
    void shouldReturnConflictWhenChangingDisponibilidadeOfDeletedProduto() throws Exception {
        when(produtoService.atualizarDisponibilidade(eq(6L), any(AtualizarDisponibilidadeProdutoDTO.class)))
                .thenThrow(new IllegalStateException("Não é possível alterar a disponibilidade de um produto apagado."));

        mockMvc.perform(patch("/api/produtos/6/disponibilidade")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(disponibilidadeDTO(false))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Não é possível alterar a disponibilidade de um produto apagado."));
    }

    @Test
    @DisplayName("I: GET /api/produtos/{id} com id valido retorna 200")
    void shouldReturnProdutoWhenIdIsValid() throws Exception {
        when(produtoService.obterProdutoPorId(3L))
                .thenReturn(produto(3L, "Sopa", 3.5, "Sopa do dia", true, false));

        mockMvc.perform(get("/api/produtos/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.nome").value("Sopa"))
                .andExpect(jsonPath("$.apagado").value(false));
    }

    @Test
    @DisplayName("J: GET /api/produtos/{id} com id inexistente retorna 404")
    void shouldReturnNotFoundWhenProdutoIdDoesNotExist() throws Exception {
        when(produtoService.obterProdutoPorId(anyLong()))
                .thenThrow(new IllegalArgumentException("O produto não foi encontrado."));

        mockMvc.perform(get("/api/produtos/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Produto não encontrado"));
    }

    @Test
    @DisplayName("K: GET /api/produtos?nome pesquisa por nome")
    void shouldSearchProdutosByNome() throws Exception {
        when(produtoService.procurarProdutoPorNome("massa"))
                .thenReturn(List.of(
                        produto(1L, "Massa Carbonara", 11.0, "Clássica", true, false),
                        produto(2L, "Massa Pesto", 10.0, "Verde", true, false)
                ));

        mockMvc.perform(get("/api/produtos").param("nome", "massa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Massa Carbonara"))
                .andExpect(jsonPath("$[1].nome").value("Massa Pesto"));

        verify(produtoService).procurarProdutoPorNome("massa");
    }

    @Test
    @DisplayName("L: GET /api/produtos?ativos=true devolve apenas não apagados")
    void shouldReturnOnlyActiveProdutos() throws Exception {
        when(produtoService.listarProdutosAtivos())
                .thenReturn(List.of(
                        produto(7L, "Salada", 6.0, "Fresca", true, false),
                        produto(8L, "Bife", 15.0, "Grelhado", false, false)
                ));

        mockMvc.perform(get("/api/produtos").param("ativos", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].apagado").value(false))
                .andExpect(jsonPath("$[1].apagado").value(false));

        verify(produtoService).listarProdutosAtivos();
    }

    @Test
    @DisplayName("M: GET /api/produtos?disponiveis=true devolve apenas disponíveis")
    void shouldReturnOnlyAvailableProdutos() throws Exception {
        when(produtoService.listarProdutosDisponiveis())
                .thenReturn(List.of(
                        produto(9L, "Peixe", 13.0, "Assado", true, false),
                        produto(10L, "Arroz", 4.0, "Branco", true, false)
                ));

        mockMvc.perform(get("/api/produtos").param("disponiveis", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].disponivel").value(true))
                .andExpect(jsonPath("$[1].disponivel").value(true));

        verify(produtoService).listarProdutosDisponiveis();
    }

    @Test
    @DisplayName("N: DELETE /api/produtos/{id} com sucesso retorna 204")
    void shouldDeleteProdutoWhenIdExists() throws Exception {
        mockMvc.perform(delete("/api/produtos/12"))
                .andExpect(status().isNoContent());

        verify(produtoService).apagarProduto(12L);
    }

    @Test
    @DisplayName("O: DELETE /api/produtos/{id} com id inexistente retorna 404")
    void shouldReturnNotFoundWhenDeletingMissingProduto() throws Exception {
        org.mockito.Mockito.doThrow(new IllegalArgumentException("O produto não foi encontrado."))
                .when(produtoService).apagarProduto(404L);

        mockMvc.perform(delete("/api/produtos/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Produto não encontrado"));
    }

    @Test
    @DisplayName("P: GET /api/produtos/pesquisa com filtro nome retorna produtos")
    void shouldSearchProdutosByNomeViaSearchEndpoint() throws Exception {
        when(produtoService.pesquisarProdutos("Pizza", null, null, null, null, null))
                .thenReturn(List.of(
                        produto(1L, "Pizza Margherita", 12.0, "Clássica", true, false),
                        produto(2L, "Pizza Pepperoni", 13.0, "Com pepperoni", true, false)
                ));

        mockMvc.perform(get("/api/produtos/pesquisa").param("nome", "Pizza"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Pizza Margherita"))
                .andExpect(jsonPath("$[1].nome").value("Pizza Pepperoni"));

        verify(produtoService).pesquisarProdutos("Pizza", null, null, null, null, null);
    }

    @Test
    @DisplayName("Q: GET /api/produtos/pesquisa com filtro preco retorna produtos com preço menor")
    void shouldSearchProdutosByPreco() throws Exception {
        when(produtoService.pesquisarProdutos(null, 10.0, null, null, null, null))
                .thenReturn(List.of(
                        produto(3L, "Hamburguer", 8.0, "Simples", true, false),
                        produto(4L, "Sopa", 6.0, "Do dia", true, false)
                ));

        mockMvc.perform(get("/api/produtos/pesquisa").param("preco", "10.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].preco").value(8.0))
                .andExpect(jsonPath("$[1].preco").value(6.0));

        verify(produtoService).pesquisarProdutos(null, 10.0, null, null, null, null);
    }

    @Test
    @DisplayName("R: GET /api/produtos/pesquisa com filtro menuId (categoria) retorna produtos do menu")
    void shouldSearchProdutosByMenuId() throws Exception {
        when(produtoService.pesquisarProdutos(null, null, 1L, null, null, null))
                .thenReturn(List.of(
                        produto(5L, "Massa Carbonara", 11.0, "Massa", true, false),
                        produto(6L, "Massa Bolonhesa", 10.5, "Massa", true, false)
                ));

        mockMvc.perform(get("/api/produtos/pesquisa").param("menuId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Massa Carbonara"))
                .andExpect(jsonPath("$[1].nome").value("Massa Bolonhesa"));

        verify(produtoService).pesquisarProdutos(null, null, 1L, null, null, null);
    }

    @Test
    @DisplayName("S: GET /api/produtos/pesquisa com filtro disponivel retorna apenas produtos disponíveis")
    void shouldSearchProdutosByDisponibilidade() throws Exception {
        when(produtoService.pesquisarProdutos(null, null, null, true, null, null))
                .thenReturn(List.of(
                        produto(7L, "Peixe Grelhado", 14.0, "Peixe", true, false),
                        produto(8L, "Camarão", 16.0, "Marisco", true, false)
                ));

        mockMvc.perform(get("/api/produtos/pesquisa").param("disponivel", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].disponivel").value(true))
                .andExpect(jsonPath("$[1].disponivel").value(true));

        verify(produtoService).pesquisarProdutos(null, null, null, true, null, null);
    }

    @Test
    @DisplayName("T: GET /api/produtos/pesquisa com filtro popularidade (data) retorna produtos pedidos nesse período")
    void shouldSearchProdutosByPopularidade() throws Exception {
        LocalDate inicio = LocalDate.of(2024, 1, 1);
        LocalDate fim = LocalDate.of(2024, 12, 31);

        when(produtoService.pesquisarProdutos(null, null, null, null, inicio, fim))
                .thenReturn(List.of(
                        produto(9L, "Frango Assado", 9.0, "Frango", true, false)
                ));

        mockMvc.perform(get("/api/produtos/pesquisa")
                .param("dataInicio", "2024-01-01")
                .param("dataFim", "2024-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Frango Assado"));

        verify(produtoService).pesquisarProdutos(null, null, null, null, inicio, fim);
    }

    @Test
    @DisplayName("U: GET /api/produtos/pesquisa com múltiplos filtros retorna produtos que satisfazem todos")
    void shouldSearchProdutosByMultipleFilters() throws Exception {
        when(produtoService.pesquisarProdutos("Pasta", 12.0, 1L, true, null, null))
                .thenReturn(List.of(
                        produto(10L, "Pasta Primavera", 11.0, "Pasta", true, false)
                ));

        mockMvc.perform(get("/api/produtos/pesquisa")
                .param("nome", "Pasta")
                .param("preco", "12.0")
                .param("menuId", "1")
                .param("disponivel", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Pasta Primavera"))
                .andExpect(jsonPath("$[0].preco").value(11.0));

        verify(produtoService).pesquisarProdutos("Pasta", 12.0, 1L, true, null, null);
    }

    @Test
    @DisplayName("V: GET /api/produtos/pesquisa sem filtros retorna lista vazia ou todos os produtos")
    void shouldReturnEmptyWhenNoFiltersProvided() throws Exception {
        when(produtoService.pesquisarProdutos(null, null, null, null, null, null))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/produtos/pesquisa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(produtoService).pesquisarProdutos(null, null, null, null, null, null);
    }


        // ********** Funções auxiliares **********

    private CriarProdutoDTO criarProdutoDTO() {
        CriarProdutoDTO dto = new CriarProdutoDTO();
        dto.setNome("Massa");
        dto.setPreco(10.0);
        dto.setDescricao("Massa fresca");
        return dto;
    }

    private Map<String, Object> produtoUpdatePayload(String nome, double preco, String descricao) {
        return Map.of(
                "id", 1L,
                "nome", nome,
                "preco", preco,
                "descricao", descricao
        );
    }

    private AtualizarDisponibilidadeProdutoDTO disponibilidadeDTO(boolean disponivel) {
        AtualizarDisponibilidadeProdutoDTO dto = new AtualizarDisponibilidadeProdutoDTO();
        dto.setDisponivel(disponivel);
        return dto;
    }

    private Produto produto(Long id, String nome, double preco, String descricao, boolean disponivel, boolean apagado) {
                Produto produto = new Produto(nome, preco, descricao, restauranteDummy());
        ReflectionTestUtils.setField(produto, "id", id);

        if (!disponivel) {
            produto.indisponibilizar();
        }
        if (apagado) {
            produto.apagar();
        }

        return produto;
    }

        private Restaurante restauranteDummy() {
                Morada morada = new Morada();
                morada.setRua("Rua Teste");
                morada.setNumero(1);
                morada.setCodigoPostal("1000-100");
                morada.setCidade("Lisboa");
                morada.setDistrito("Lisboa");
                morada.setPais("Portugal");

                return new Restaurante(
                                "Restaurante Teste",
                                "123456789",
                                morada,
                                new RestauranteAdmin("Admin", "admin@teste.pt", "pw"),
                                new CatalogoEntregas()
                );
        }

}