// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################

package pt.ul.fc.css.tascaeats.controllers;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@WebMvcTest(WebUtilizadorController.class)
class WebUtilizadorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RestTemplate restTemplate;

    @Test
    void showLoginForm_ReturnsLoginView() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void processLogin_WithValidCredentials_RedirectsToHome() throws Exception {
        when(restTemplate.postForEntity(eq("http://localhost:80/api/login"), any(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(Map.of("message", "Login successful")));
        when(restTemplate.getForObject(eq("http://localhost:80/api/Utilizadors"), eq(List.class)))
                .thenReturn(List.of(Map.of(
                        "id", 1,
                        "name", "Cliente",
                        "email", "cliente@tascaeats.local",
                        "type", "Cliente"
                )));

        mockMvc.perform(post("/login")
                .param("email", "cliente@tascaeats.local")
                .param("password", "cliente123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/restaurantes")); 
    }

    @Test
    void processLogin_WithInvalidCredentials_ReturnsLoginViewWithError() throws Exception {
        when(restTemplate.postForEntity(eq("http://localhost:80/api/login"), any(), eq(Map.class)))
                .thenThrow(HttpClientErrorException.create(
                        HttpStatus.UNAUTHORIZED,
                        "Unauthorized",
                        null,
                        null,
                        null
                ));

        mockMvc.perform(post("/login")
                .param("email", "wrong@email.com")
                .param("password", "wrongpass"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("error", "Nao existe nenhuma conta com esse email."));
    }

    @Test
    void showRegistrationForm_ReturnsRegisterView() throws Exception {
        mockMvc.perform(get("/registo"))
                .andExpect(status().isOk())
                .andExpect(view().name("registo"));
    }

    @Test
    void processRegistration_WithValidData_RedirectsToLogin() throws Exception {
        when(restTemplate.postForEntity(eq("http://localhost:80/api/register"), any(), eq(Map.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                        "id", 1,
                        "email", "novo@tasca.local",
                        "type", "CLIENTE"
                )));

        mockMvc.perform(post("/registo")
                .param("nome", "Novo Cliente")
                .param("email", "novo@tasca.local")
                .param("password", "senhaSegura")
                .param("tipo", "CLIENTE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}
