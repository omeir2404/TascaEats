package pt.ul.fc.css.tascaeats.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.ul.fc.css.tascaeats.domain.user.Client;
import pt.ul.fc.css.tascaeats.domain.user.User;
import pt.ul.fc.css.tascaeats.dto.UserLoginDTO;
import pt.ul.fc.css.tascaeats.dto.UserRegistrationDTO;
import pt.ul.fc.css.tascaeats.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_shouldCreateClientWithAddress() {
        UserRegistrationDTO dto = new UserRegistrationDTO(
                "Ana",
                "ana@example.com",
                "secret",
                "client",
                "Rua Principal"
        );

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User created = userService.createUser(dto);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        assertThat(created).isInstanceOf(Client.class);
        assertThat(created.getName()).isEqualTo("Ana");
        assertThat(created.getEmail()).isEqualTo("ana@example.com");
        assertThat(created.getPassword()).isEqualTo("secret");
        assertThat(((Client) created).getAddress()).isNotNull();
        assertThat(((Client) created).getAddress().getStreet()).isEqualTo("Rua Principal");
        assertThat(captor.getValue()).isSameAs(created);
    }

    @Test
    void createUser_shouldRejectDuplicateEmail() {
        UserRegistrationDTO dto = new UserRegistrationDTO(
                "Ana",
                "ana@example.com",
                "secret",
                "client",
                null
        );

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(new Client()));

        assertThatThrownBy(() -> userService.createUser(dto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Email already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_shouldReturnTrueWhenEmailExists() {
        UserLoginDTO dto = new UserLoginDTO("ana@example.com", "anything");
        when(authService.login(dto.getEmail())).thenReturn(new Client());

        assertThat(userService.login(dto)).isTrue();
    }

    @Test
    void login_shouldReturnFalseForBlankEmail() {
        UserLoginDTO dto = new UserLoginDTO("  ", "anything");

        assertThat(userService.login(dto)).isFalse();
        verify(authService, never()).login("  ");
    }

    @Test
    void updateUser_shouldReturnNullWhenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        User updated = userService.updateUser(99L, new UserRegistrationDTO("Ana", "ana@example.com", "secret", "client", null));

        assertThat(updated).isNull();
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_shouldDeleteWhenUserExists() {
        when(userRepository.existsById(10L)).thenReturn(true);

        boolean deleted = userService.deleteUser(10L);

        assertThat(deleted).isTrue();
        verify(userRepository).deleteById(10L);
    }

    @Test
    void getAllUsers_shouldDelegateToRepository() {
        when(userRepository.findAll()).thenReturn(List.of(new Client()));

        List<User> users = userService.getAllUsers();

        assertThat(users).hasSize(1);
    }
}


