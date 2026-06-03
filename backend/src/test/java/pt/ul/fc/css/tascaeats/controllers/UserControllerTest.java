// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pt.ul.fc.css.tascaeats.domain.utilizador.Cliente;
import pt.ul.fc.css.tascaeats.domain.utilizador.Morada;
import pt.ul.fc.css.tascaeats.dto.utilizadorDTO.UtilizadorLoginDTO;
import pt.ul.fc.css.tascaeats.dto.utilizadorDTO.UtilizadorRegistoDTO;
import pt.ul.fc.css.tascaeats.dto.utilizadorDTO.UtilizadorUpdateDTO;
import pt.ul.fc.css.tascaeats.service.utilizadorServices.UtilizadorService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UtilizadorController.class)
class UtilizadorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UtilizadorService UtilizadorService;

    // Registration
    @Test
    @DisplayName("Case: valid registration escapes returned email")
    void shouldEscapeReturnedEmailWhenRegistrationIsValid() throws Exception {
        Cliente created = new Cliente();
        created.setId(1L);
        created.setName("Ana");
        created.setEmail("<script>alert(1)</script>@example.com");
        created.setPassword("secret");

        when(UtilizadorService.createUtilizador(any(UtilizadorRegistoDTO.class))).thenReturn(created);

        UtilizadorRegistoDTO dto = new UtilizadorRegistoDTO(
                "Ana",
                "<script>alert(1)</script>@example.com",
                "secret",
                "client",
                null
        );

        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Utilizador registered successfully"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("&lt;script&gt;alert(1)&lt;/script&gt;@example.com"))
                .andExpect(jsonPath("$.type").value("Cliente"));
    }

    @Test
    @DisplayName("Case: invalid registration returns bad request")
    void shouldReturnBadRequestWhenRegistrationDataIsInvalid() throws Exception {
        when(UtilizadorService.createUtilizador(any(UtilizadorRegistoDTO.class))).thenThrow(new IllegalArgumentException("invalid"));

        UtilizadorRegistoDTO dto = new UtilizadorRegistoDTO("Ana", "ana@example.com", "secret", "client", null);

        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid registration data"));
    }

    @Test
    @DisplayName("Case: invalid registration returns conflict when email already exists")
    void shouldReturnConflictWhenRegistrationEmailAlreadyExists() throws Exception {
        when(UtilizadorService.createUtilizador(any(UtilizadorRegistoDTO.class))).thenThrow(new IllegalStateException("exists"));

        UtilizadorRegistoDTO dto = new UtilizadorRegistoDTO("Ana", "ana@example.com", "secret", "client", null);

        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Utilizador already exists"));
    }

    // Login
    @Test
    @DisplayName("Case: invalid login returns unauthorized")
    void shouldReturnUnauthorizedWhenLoginIsInvalid() throws Exception {
        when(UtilizadorService.login(any(UtilizadorLoginDTO.class))).thenReturn(false);

        UtilizadorLoginDTO dto = new UtilizadorLoginDTO("ana@example.com", "wrong");

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid Utilizador"));
    }

    @Test
    @DisplayName("Case: valid login returns success message")
    void shouldReturnSuccessMessageWhenLoginIsValid() throws Exception {
        when(UtilizadorService.login(any(UtilizadorLoginDTO.class))).thenReturn(true);

        UtilizadorLoginDTO dto = new UtilizadorLoginDTO("ana@example.com", "secret");

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful (mock)"));
    }

    // Manage Utilizadors
    @Test
    @DisplayName("Case: manage Utilizadors get all Utilizadors returns list")
    void shouldReturnUtilizadorsListWhenGettingAllUtilizadors() throws Exception {
        Cliente Utilizador = new Cliente();
        Utilizador.setId(1L);
        Utilizador.setName("Ana");
        Utilizador.setEmail("ana@example.com");
        Morada address = new Morada();
        address.setRua("Rua A");
        Utilizador.setAddress(address);
        when(UtilizadorService.getAllUtilizadors()).thenReturn(List.of(Utilizador));

        mockMvc.perform(get("/api/Utilizadors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("ana@example.com"))
            .andExpect(jsonPath("$[0].address.rua").value("Rua A"))
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    @Test
    @DisplayName("Case: manage Utilizadors get Utilizador by id returns dto when found")
    void shouldReturnUtilizadorDtoWhenGettingUtilizadorByIdAndFound() throws Exception {
        Cliente Utilizador = new Cliente();
        Utilizador.setId(1L);
        Utilizador.setName("Ana");
        Utilizador.setEmail("ana@example.com");
        when(UtilizadorService.getUtilizadorById(1L)).thenReturn(Utilizador);

        mockMvc.perform(get("/api/Utilizadors/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.type").value("Cliente"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("Case: manage Utilizadors get Utilizador by id returns not found when missing")
    void shouldReturnNotFoundWhenGettingUtilizadorByIdAndMissing() throws Exception {
        when(UtilizadorService.getUtilizadorById(anyLong())).thenReturn(null);

        mockMvc.perform(get("/api/Utilizadors/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Utilizador not found"));
    }

    @Test
    @DisplayName("Case: manage Utilizadors delete Utilizador returns no content when deleted")
    void shouldReturnNoContentWhenDeletingExistingUtilizador() throws Exception {
        when(UtilizadorService.deleteUtilizador(anyLong())).thenReturn(true);

        mockMvc.perform(delete("/api/Utilizadors/10"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Case: manage Utilizadors delete Utilizador returns not found when missing")
    void shouldReturnNotFoundWhenDeletingMissingUtilizador() throws Exception {
        when(UtilizadorService.deleteUtilizador(anyLong())).thenReturn(false);

        mockMvc.perform(delete("/api/Utilizadors/10"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Utilizador not found"));
    }

    @Test
    @DisplayName("Case: manage Utilizadors update Utilizador returns ok when updated")
    void shouldReturnOkWhenUpdatingUtilizadorSuccessfully() throws Exception {
        Cliente updated = new Cliente();
        updated.setId(1L);
        updated.setName("Ana Updated");
        updated.setEmail("ana.updated@example.com");
        when(UtilizadorService.updateUtilizador(anyLong(), any(UtilizadorUpdateDTO.class))).thenReturn(updated);

        UtilizadorUpdateDTO dto = new UtilizadorUpdateDTO("Ana Updated", "ana.updated@example.com", "secret", "client", null, false);

        mockMvc.perform(put("/api/Utilizadors/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ana Updated"));
    }

    @Test
    @DisplayName("Case: manage Utilizadors update Utilizador returns conflict when email exists")
    void shouldReturnConflictWhenUpdatingUtilizadorAndServiceThrowsConflict() throws Exception {
        when(UtilizadorService.updateUtilizador(anyLong(), any(UtilizadorUpdateDTO.class))).thenThrow(new IllegalStateException("conflict"));

        UtilizadorUpdateDTO dto = new UtilizadorUpdateDTO("Ana", "ana@example.com", "secret", "client", null, false);

        mockMvc.perform(put("/api/Utilizadors/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Utilizador update conflict"));
    }
}


