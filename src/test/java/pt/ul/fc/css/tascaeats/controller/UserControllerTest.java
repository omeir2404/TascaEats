package pt.ul.fc.css.tascaeats.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pt.ul.fc.css.tascaeats.domain.user.Client;
import pt.ul.fc.css.tascaeats.dto.UserLoginDTO;
import pt.ul.fc.css.tascaeats.dto.UserRegistrationDTO;
import pt.ul.fc.css.tascaeats.service.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void register_shouldEscapeReturnedEmail() throws Exception {
        Client created = new Client();
        created.setId(1L);
        created.setName("Ana");
        created.setEmail("<script>alert(1)</script>@example.com");
        created.setPassword("secret");

        when(userService.createUser(any(UserRegistrationDTO.class))).thenReturn(created);

        UserRegistrationDTO dto = new UserRegistrationDTO(
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
                .andExpect(jsonPath("$.email").value("&lt;script&gt;alert(1)&lt;/script&gt;@example.com"))
                .andExpect(jsonPath("$.type").value("Client"));
    }

    @Test
    void login_shouldReturnUnauthorizedWhenInvalid() throws Exception {
        when(userService.login(any(UserLoginDTO.class))).thenReturn(false);

        UserLoginDTO dto = new UserLoginDTO("ana@example.com", "wrong");

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid user"));
    }

    @Test
    void getUsers_shouldReturnList() throws Exception {
        Client user = new Client();
        user.setId(1L);
        user.setName("Ana");
        user.setEmail("ana@example.com");
        when(userService.getAllUsers()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("ana@example.com"));
    }

    @Test
    void getUserById_shouldReturnNotFoundWhenMissing() throws Exception {
        when(userService.getUserById(anyLong())).thenReturn(null);

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found"));
    }

    @Test
    void deleteUser_shouldReturnNoContentWhenDeleted() throws Exception {
        when(userService.deleteUser(anyLong())).thenReturn(true);

        mockMvc.perform(delete("/api/users/10"))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateUser_shouldReturnOkWhenUpdated() throws Exception {
        Client updated = new Client();
        updated.setId(1L);
        updated.setName("Ana Updated");
        updated.setEmail("ana.updated@example.com");
        when(userService.updateUser(anyLong(), any(UserRegistrationDTO.class))).thenReturn(updated);

        UserRegistrationDTO dto = new UserRegistrationDTO("Ana Updated", "ana.updated@example.com", "secret", "client", null);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ana Updated"));
    }
}


