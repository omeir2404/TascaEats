package pt.ul.fc.css.tascaeats.controllers;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import pt.ul.fc.css.tascaeats.ui.AppState;
import pt.ul.fc.css.tascaeats.ui.RestauranteApiClient;
import pt.ul.fc.css.tascaeats.ui.RestauranteResumo;

@WebMvcTest(WebRestauranteController.class)
class WebRestauranteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RestauranteApiClient restauranteApiClient;

    @MockBean
    private AppState appState;

    @Test
    void listRestaurantes_ReturnsRestaurantesViewWithList() throws Exception {
        // Arrange: Criar o record com os parâmetros corretos
        RestauranteResumo mockRestaurante = new RestauranteResumo(1L, "Tasca do Zé", "ABERTO");
        List<RestauranteResumo> mockList = Collections.singletonList(mockRestaurante);
        
        // Mock do método exato que existe no cliente
        when(restauranteApiClient.listarRestaurantes(nullable(String.class), nullable(String.class)))
                .thenReturn(mockList);

        // Act & Assert
        mockMvc.perform(get("/restaurantes"))
                .andExpect(status().isOk())
                .andExpect(view().name("restaurantes"))
                .andExpect(model().attributeExists("restaurantes"));
    }
}