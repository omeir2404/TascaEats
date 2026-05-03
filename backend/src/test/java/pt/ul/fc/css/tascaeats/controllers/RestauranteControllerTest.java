// ##############################################################
// 					TASCAEATS      			         			#
// 					CSS GRUPO: 25	     			    		#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.controllers;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import pt.ul.fc.css.tascaeats.domain.restaurante.CatalogoEntregas;
import pt.ul.fc.css.tascaeats.domain.restaurante.EstadoRestaurante;
import pt.ul.fc.css.tascaeats.domain.restaurante.Restaurante;
import pt.ul.fc.css.tascaeats.domain.restaurante.TipoRestaurante;
import pt.ul.fc.css.tascaeats.domain.utilizador.Morada;
import pt.ul.fc.css.tascaeats.domain.utilizador.RestauranteAdmin;
import pt.ul.fc.css.tascaeats.dto.moradaDTO.MoradaDTO;
import pt.ul.fc.css.tascaeats.dto.restauranteDTO.AtualizarEstadoRestauranteDTO;
import pt.ul.fc.css.tascaeats.dto.restauranteDTO.AtualizarRestauranteDTO;
import pt.ul.fc.css.tascaeats.dto.restauranteDTO.CriarRestauranteDTO;
import pt.ul.fc.css.tascaeats.service.restauranteServices.RestauranteService;

@WebMvcTest(RestauranteController.class)
class RestauranteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RestauranteService restauranteService;

    @Test
    @DisplayName("A: POST /api/restaurantes com payload válido retorna 201.")
    void shouldCreateRestauranteWhenPayloadIsValid() throws Exception {
        CriarRestauranteDTO dto = criarRestauranteDTO("Tasca", "123456789");
        Restaurante restaurante = restaurante(1L, "Tasca", "123456789", EstadoRestaurante.FECHADO);

        when(restauranteService.criarRestaurante(any(CriarRestauranteDTO.class), eq("admin@tasca.pt")))
                .thenReturn(restaurante);

        mockMvc.perform(post("/api/restaurantes")
                        .param("adminEmail", "admin@tasca.pt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Tasca"))
                .andExpect(jsonPath("$.nif").value("123456789"))
                .andExpect(jsonPath("$.estado").value("FECHADO"))
                .andExpect(jsonPath("$.morada.rua").value("Rua da Tasca"));
    }

    @Test
    @DisplayName("B: POST /api/restaurantes sem adminEmail retorna 400.")
    void shouldReturnBadRequestWhenAdminEmailIsMissing() throws Exception {
        CriarRestauranteDTO dto = criarRestauranteDTO("Tasca", "123456789");

        mockMvc.perform(post("/api/restaurantes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("C: POST /api/restaurantes com NIF inválido ou duplicado retorna 400.")
    void shouldReturnBadRequestWhenNifIsInvalidOrDuplicated() throws Exception {
        CriarRestauranteDTO dto = criarRestauranteDTO("Tasca", "123");

        when(restauranteService.criarRestaurante(any(CriarRestauranteDTO.class), eq("admin@tasca.pt")))
                .thenThrow(new IllegalArgumentException("Nif invalido."));

        mockMvc.perform(post("/api/restaurantes")
                        .param("adminEmail", "admin@tasca.pt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Nif invalido."));
    }

    @Test
    @DisplayName("D: PUT /api/restaurantes/{id} com sucesso retorna 200.")
    void shouldUpdateRestauranteWhenIdExists() throws Exception {
        AtualizarRestauranteDTO dto = atualizarRestauranteDTO("Tasca Nova", "987654321");
        Restaurante restaurante = restaurante(5L, "Tasca Nova", "987654321", EstadoRestaurante.FECHADO);

        when(restauranteService.atualizarRestaurante(eq(5L), any(AtualizarRestauranteDTO.class)))
                .thenReturn(restaurante);

        mockMvc.perform(put("/api/restaurantes/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.nome").value("Tasca Nova"))
                .andExpect(jsonPath("$.nif").value("987654321"));
    }

    @Test
    @DisplayName("E: PUT /api/restaurantes/{id} inexistente retorna 404.")
    void shouldReturnNotFoundWhenUpdatingMissingRestaurante() throws Exception {
        AtualizarRestauranteDTO dto = atualizarRestauranteDTO("Tasca Nova", "987654321");

        when(restauranteService.atualizarRestaurante(eq(99L), any(AtualizarRestauranteDTO.class)))
                .thenThrow(new IllegalArgumentException("O restaurante não foi encontrado."));

        mockMvc.perform(put("/api/restaurantes/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("O restaurante não foi encontrado."));
    }

    @Test
    @DisplayName("F: PATCH /api/restaurantes/{id}/estado com sucesso retorna 200.")
    void shouldUpdateEstadoWhenRequestIsValid() throws Exception {
        AtualizarEstadoRestauranteDTO dto = atualizarEstadoDTO(EstadoRestaurante.ABERTO);
        Restaurante restaurante = restaurante(3L, "Tasca Central", "123456789", EstadoRestaurante.ABERTO);

        when(restauranteService.atualizarEstado(eq(3L), any(AtualizarEstadoRestauranteDTO.class)))
                .thenReturn(restaurante);

        mockMvc.perform(patch("/api/restaurantes/3/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ABERTO"));
    }

    @Test
    @DisplayName("G: PATCH /api/restaurantes/{id}/estado repetido retorna 409")
    void shouldReturnConflictWhenEstadoIsRepeated() throws Exception {
        AtualizarEstadoRestauranteDTO dto = atualizarEstadoDTO(EstadoRestaurante.ABERTO);

        when(restauranteService.atualizarEstado(eq(3L), any(AtualizarEstadoRestauranteDTO.class)))
                .thenThrow(new IllegalStateException("O restaurante já está nesse estado."));

        mockMvc.perform(patch("/api/restaurantes/3/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("O restaurante já está nesse estado."));
    }

    @Test
    @DisplayName("H: PATCH /api/restaurantes/{id}/abrir delega para estado ABERTO")
    void shouldDelegateOpenEndpointToEstadoAberto() throws Exception {
        Restaurante restaurante = restaurante(7L, "Tasca Aberta", "123456789", EstadoRestaurante.ABERTO);

        when(restauranteService.atualizarEstado(eq(7L), any(AtualizarEstadoRestauranteDTO.class)))
                .thenReturn(restaurante);

        mockMvc.perform(patch("/api/restaurantes/7/abrir"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ABERTO"));

        ArgumentCaptor<AtualizarEstadoRestauranteDTO> captor =
                ArgumentCaptor.forClass(AtualizarEstadoRestauranteDTO.class);
        verify(restauranteService).atualizarEstado(eq(7L), captor.capture());
        org.junit.jupiter.api.Assertions.assertEquals(EstadoRestaurante.ABERTO, captor.getValue().getEstado());
    }

    @Test
    @DisplayName("I: PATCH /api/restaurantes/{id}/fechar delega para estado FECHADO")
    void shouldDelegateCloseEndpointToEstadoFechado() throws Exception {
        Restaurante restaurante = restaurante(8L, "Tasca Fechada", "123456789", EstadoRestaurante.FECHADO);

        when(restauranteService.atualizarEstado(eq(8L), any(AtualizarEstadoRestauranteDTO.class)))
                .thenReturn(restaurante);

        mockMvc.perform(patch("/api/restaurantes/8/fechar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("FECHADO"));

        ArgumentCaptor<AtualizarEstadoRestauranteDTO> captor =
                ArgumentCaptor.forClass(AtualizarEstadoRestauranteDTO.class);
        verify(restauranteService).atualizarEstado(eq(8L), captor.capture());
        org.junit.jupiter.api.Assertions.assertEquals(EstadoRestaurante.FECHADO, captor.getValue().getEstado());
    }

    @Test
    @DisplayName("J: GET /api/restaurantes/{id} valido retorna 200")
    void shouldReturnRestauranteWhenIdIsValid() throws Exception {
        Restaurante restaurante = restaurante(11L, "Tasca Eats", "123456789", EstadoRestaurante.FECHADO);

        when(restauranteService.obterRestaurantePorId(11L)).thenReturn(restaurante);

        mockMvc.perform(get("/api/restaurantes/11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(11))
                .andExpect(jsonPath("$.nome").value("Tasca Eats"));
    }

    @Test
    @DisplayName("K: GET /api/restaurantes/{id} invalido retorna 404")
    void shouldReturnNotFoundWhenIdIsInvalid() throws Exception {
        when(restauranteService.obterRestaurantePorId(anyLong()))
                .thenThrow(new IllegalArgumentException("O restaurante não foi encontrado."));

        mockMvc.perform(get("/api/restaurantes/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("O restaurante não foi encontrado."));
    }

    @Test
    @DisplayName("K1: DELETE /api/restaurantes/{id} remove restaurante com sucesso")
    void shouldRemoveRestauranteWhenIdExists() throws Exception {
        when(restauranteService.removerRestaurante(12L)).thenReturn(true);

        mockMvc.perform(delete("/api/restaurantes/12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Restaurante removido com sucesso."));

        verify(restauranteService).removerRestaurante(12L);
    }

    @Test
    @DisplayName("L: GET /api/restaurantes?nome pesquisa por nome")
    void shouldSearchRestaurantesByNome() throws Exception {
        when(restauranteService.procurarRestaurantePorNome("tasca"))
                .thenReturn(List.of(
                        restaurante(1L, "Tasca Azul", "123456789", EstadoRestaurante.FECHADO),
                        restaurante(2L, "Tasca Verde", "987654321", EstadoRestaurante.ABERTO)
                ));

        mockMvc.perform(get("/api/restaurantes").param("nome", "tasca"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Tasca Azul"))
                .andExpect(jsonPath("$[1].nome").value("Tasca Verde"));

        verify(restauranteService).procurarRestaurantePorNome("tasca");
    }

    @Test
        @DisplayName("M: GET /api/restaurantes?estado=ABERTO filtra por estado")
    void shouldFilterRestaurantesByEstado() throws Exception {
        when(restauranteService.listarPorEstado(EstadoRestaurante.ABERTO))
                .thenReturn(List.of(restaurante(4L, "Tasca Sol", "123456789", EstadoRestaurante.ABERTO)));

        mockMvc.perform(get("/api/restaurantes").param("estado", "ABERTO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estado").value("ABERTO"))
                .andExpect(jsonPath("$[0].nome").value("Tasca Sol"));

        verify(restauranteService).listarPorEstado(EstadoRestaurante.ABERTO);
    }

    @Test
    @DisplayName("N: GET /api/restaurantes sem filtros devolve lista completa")
    void shouldReturnAllRestaurantesWhenNoFiltersAreProvided() throws Exception {
        when(restauranteService.listarRestaurantes())
                .thenReturn(List.of(
                        restaurante(1L, "Tasca A", "123456789", EstadoRestaurante.FECHADO),
                        restaurante(2L, "Tasca B", "987654321", EstadoRestaurante.ABERTO)
                ));

        mockMvc.perform(get("/api/restaurantes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Tasca A"))
                .andExpect(jsonPath("$[1].nome").value("Tasca B"));

        verify(restauranteService).listarRestaurantes();
    }

    @Test
    @DisplayName("O: GET /api/restaurantes/pesquisa?nome usa o endpoint de pesquisa")
    void shouldSearchRestaurantesThroughPesquisaEndpoint() throws Exception {
        when(restauranteService.pesquisarRestaurantes("tasca", null, null, null, null, null, null, null))
                .thenReturn(List.of(restaurante(9L, "Tasca Pesquisa", "123456789", EstadoRestaurante.ABERTO)));

        mockMvc.perform(get("/api/restaurantes/pesquisa").param("nome", "tasca"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Tasca Pesquisa"))
                .andExpect(jsonPath("$[0].estado").value("ABERTO"));

        verify(restauranteService).pesquisarRestaurantes("tasca", null, null, null, null, null, null, null);
    }

    @Test
    @DisplayName("P: GET /api/restaurantes/pesquisa aceita filtros avancados")
    void shouldAcceptAdvancedPesquisaParameters() throws Exception {
        when(restauranteService.pesquisarRestaurantes(
                "tasca",
                TipoRestaurante.ITALIANO,
                EstadoRestaurante.ABERTO,
                10,
                4.5,
                null,
                "Lisboa",
                20.0
        )).thenReturn(List.of(restaurante(10L, "Tasca Filtro", "987654321", EstadoRestaurante.ABERTO)));

        mockMvc.perform(get("/api/restaurantes/pesquisa")
                        .param("nome", "tasca")
                        .param("tipoCozinha", "ITALIANO")
                        .param("estado", "ABERTO")
                        .param("pedidosRealizados", "10")
                        .param("avaliacaoMinima", "4.5")
                        .param("morada", "Lisboa")
                        .param("precoMedio", "20.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Tasca Filtro"));

        verify(restauranteService).pesquisarRestaurantes(
                "tasca",
                TipoRestaurante.ITALIANO,
                EstadoRestaurante.ABERTO,
                10,
                4.5,
                null,
                "Lisboa",
                20.0
        );
    }


    //**********Funções auxiliares************

    private CriarRestauranteDTO criarRestauranteDTO(String nome, String nif) {
        CriarRestauranteDTO dto = new CriarRestauranteDTO();
        dto.setNome(nome);
        dto.setNif(nif);
        dto.setMorada(moradaDTO());
        return dto;
    }

    private AtualizarRestauranteDTO atualizarRestauranteDTO(String nome, String nif) {
        AtualizarRestauranteDTO dto = new AtualizarRestauranteDTO();
        dto.setNome(nome);
        dto.setNif(nif);
        dto.setMorada(moradaDTO());
        return dto;
    }

    private AtualizarEstadoRestauranteDTO atualizarEstadoDTO(EstadoRestaurante estado) {
        AtualizarEstadoRestauranteDTO dto = new AtualizarEstadoRestauranteDTO();
        dto.setEstado(estado);
        return dto;
    }

    private MoradaDTO moradaDTO() {
        return new MoradaDTO("Rua da Tasca", 10, null, "1000-100", "Lisboa", "Lisboa", "Portugal");
    }

    private Restaurante restaurante(Long id, String nome, String nif, EstadoRestaurante estado) {
        Morada morada = new Morada();
        morada.setRua("Rua da Tasca");
        morada.setNumero(10);
        morada.setCodigoPostal("1000-100");
        morada.setCidade("Lisboa");
        morada.setComplemento("1º Dir");
        morada.setDistrito("Lisboa");
        morada.setPais("Portugal");

        Restaurante restaurante = new Restaurante(
                nome,
                nif,
                morada,
                new RestauranteAdmin("Admin", "admin@tasca.pt", "segredo"),
                new CatalogoEntregas()
        );
        ReflectionTestUtils.setField(restaurante, "id", id);

        if (estado == EstadoRestaurante.ABERTO) {
            restaurante.abrir();
        } else if (estado == EstadoRestaurante.RESERVADO) {
            restaurante.reservar();
        }

        return restaurante;
    }

}
