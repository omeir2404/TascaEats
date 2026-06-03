package pt.ul.fc.css.tascaeats.controllers;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import pt.ul.fc.css.tascaeats.ui.AppState;
import pt.ul.fc.css.tascaeats.ui.UtilizadorApiClient;

@WebMvcTest(WebUtilizadorController.class)
class WebUtilizadorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UtilizadorApiClient utilizadorApiClient;

    @MockBean
    private AppState appState;

    @Test
    void showLoginForm_ReturnsLoginView() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void processLogin_WithValidCredentials_RedirectsToHome() throws Exception {
        // Mock do LoginResult real
        UtilizadorApiClient.LoginResult sucesso = new UtilizadorApiClient.LoginResult(true, false, "Sucesso");
        when(utilizadorApiClient.login(anyString(), anyString())).thenReturn(sucesso);

        mockMvc.perform(post("/login")
                .param("email", "cliente@tascaeats.local")
                .param("password", "cliente123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/restaurantes")); 
    }

    @Test
    void processLogin_WithInvalidCredentials_ReturnsLoginViewWithError() throws Exception {
        // Mock de falha
        UtilizadorApiClient.LoginResult falha = new UtilizadorApiClient.LoginResult(false, false, "Credenciais invalidas");
        when(utilizadorApiClient.login(anyString(), anyString())).thenReturn(falha);

        mockMvc.perform(post("/login")
                .param("email", "wrong@email.com")
                .param("password", "wrongpass"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("error")); 
    }

    @Test
    void showRegistrationForm_ReturnsRegisterView() throws Exception {
        mockMvc.perform(get("/registo"))
                .andExpect(status().isOk())
                .andExpect(view().name("registo"));
    }

    @Test
    void processRegistration_WithValidData_RedirectsToLogin() throws Exception {
        // Simular que o registo corre bem
        UtilizadorApiClient.RegistoResult sucessoRegisto = new UtilizadorApiClient.RegistoResult(1L, "novo@tasca.local", "CLIENTE", false, "Sucesso");
        
        when(utilizadorApiClient.registarUtilizador(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(sucessoRegisto);

        mockMvc.perform(post("/registo")
                .param("nome", "Novo Cliente")
                .param("email", "novo@tasca.local")
                .param("password", "senhaSegura")
                .param("tipo", "CLIENTE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}