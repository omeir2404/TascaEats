package pt.ul.fc.css.tascaeats.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pt.ul.fc.css.tascaeats.domain.user.Address;
import pt.ul.fc.css.tascaeats.domain.user.Client;
import pt.ul.fc.css.tascaeats.dto.UserLoginDTO;
import pt.ul.fc.css.tascaeats.dto.UserRegistrationDTO;
import pt.ul.fc.css.tascaeats.dto.UserUpdateDTO;
import pt.ul.fc.css.tascaeats.service.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    // Registration
    @Test
    @DisplayName("Case: valid registration escapes returned email")
    void shouldEscapeReturnedEmailWhenRegistrationIsValid() throws Exception {
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
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("&lt;script&gt;alert(1)&lt;/script&gt;@example.com"))
                .andExpect(jsonPath("$.type").value("Client"));
    }

    @Test
    @DisplayName("Case: invalid registration returns bad request")
    void shouldReturnBadRequestWhenRegistrationDataIsInvalid() throws Exception {
        when(userService.createUser(any(UserRegistrationDTO.class))).thenThrow(new IllegalArgumentException("invalid"));

        UserRegistrationDTO dto = new UserRegistrationDTO("Ana", "ana@example.com", "secret", "client", null);

        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid registration data"));
    }

    @Test
    @DisplayName("Case: invalid registration returns conflict when email already exists")
    void shouldReturnConflictWhenRegistrationEmailAlreadyExists() throws Exception {
        when(userService.createUser(any(UserRegistrationDTO.class))).thenThrow(new IllegalStateException("exists"));

        UserRegistrationDTO dto = new UserRegistrationDTO("Ana", "ana@example.com", "secret", "client", null);

        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("User already exists"));
    }

    // Login
    @Test
    @DisplayName("Case: invalid login returns unauthorized")
    void shouldReturnUnauthorizedWhenLoginIsInvalid() throws Exception {
        when(userService.login(any(UserLoginDTO.class))).thenReturn(false);

        UserLoginDTO dto = new UserLoginDTO("ana@example.com", "wrong");

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid user"));
    }

    @Test
    @DisplayName("Case: valid login returns success message")
    void shouldReturnSuccessMessageWhenLoginIsValid() throws Exception {
        when(userService.login(any(UserLoginDTO.class))).thenReturn(true);

        UserLoginDTO dto = new UserLoginDTO("ana@example.com", "secret");

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful (mock)"));
    }

    // Manage users
    @Test
    @DisplayName("Case: manage users get all users returns list")
    void shouldReturnUsersListWhenGettingAllUsers() throws Exception {
        Client user = new Client();
        user.setId(1L);
        user.setName("Ana");
        user.setEmail("ana@example.com");
        Address address = new Address();
        address.setStreet("Rua A");
        user.setAddress(address);
        when(userService.getAllUsers()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("ana@example.com"))
                .andExpect(jsonPath("$[0].address.street").value("Rua A"))
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    @Test
    @DisplayName("Case: manage users get user by id returns dto when found")
    void shouldReturnUserDtoWhenGettingUserByIdAndFound() throws Exception {
        Client user = new Client();
        user.setId(1L);
        user.setName("Ana");
        user.setEmail("ana@example.com");
        when(userService.getUserById(1L)).thenReturn(user);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.type").value("Client"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("Case: manage users get user by id returns not found when missing")
    void shouldReturnNotFoundWhenGettingUserByIdAndMissing() throws Exception {
        when(userService.getUserById(anyLong())).thenReturn(null);

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found"));
    }

    @Test
    @DisplayName("Case: manage users delete user returns no content when deleted")
    void shouldReturnNoContentWhenDeletingExistingUser() throws Exception {
        when(userService.deleteUser(anyLong())).thenReturn(true);

        mockMvc.perform(delete("/api/users/10"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Case: manage users delete user returns not found when missing")
    void shouldReturnNotFoundWhenDeletingMissingUser() throws Exception {
        when(userService.deleteUser(anyLong())).thenReturn(false);

        mockMvc.perform(delete("/api/users/10"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found"));
    }

    @Test
    @DisplayName("Case: manage users update user returns ok when updated")
    void shouldReturnOkWhenUpdatingUserSuccessfully() throws Exception {
        Client updated = new Client();
        updated.setId(1L);
        updated.setName("Ana Updated");
        updated.setEmail("ana.updated@example.com");
        when(userService.updateUser(anyLong(), any(UserUpdateDTO.class))).thenReturn(updated);

        UserUpdateDTO dto = new UserUpdateDTO("Ana Updated", "ana.updated@example.com", "secret", "client", null, false);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ana Updated"));
    }

    @Test
    @DisplayName("Case: manage users update user returns conflict when email exists")
    void shouldReturnConflictWhenUpdatingUserAndServiceThrowsConflict() throws Exception {
        when(userService.updateUser(anyLong(), any(UserUpdateDTO.class))).thenThrow(new IllegalStateException("conflict"));

        UserUpdateDTO dto = new UserUpdateDTO("Ana", "ana@example.com", "secret", "client", null, false);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("User update conflict"));
    }
}


