package pt.ul.fc.css.tascaeats.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.ul.fc.css.tascaeats.domain.user.*;
import pt.ul.fc.css.tascaeats.dto.UserLoginDTO;
import pt.ul.fc.css.tascaeats.dto.UserRegistrationDTO;
import pt.ul.fc.css.tascaeats.dto.UserUpdateDTO;
import pt.ul.fc.css.tascaeats.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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

    // Case: valid registration (client with address)
    @Test
    @DisplayName("Case: valid registration")
    void validRegistration_shouldCreateUser() {
        UserRegistrationDTO dto = registration("Ana", "ana@example.com", "secret", "client", "Rua Principal");
        when(userRepository.findByEmail("ana@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User created = userService.createUser(dto);

        assertThat(created).isInstanceOf(Client.class);
        assertThat(created.getEmail()).isEqualTo("ana@example.com");
    }

    @Test
    @DisplayName("Case: invalid registration (email already exists)")
    void invalidRegistration_emailAlreadyExists_shouldFail() {
        UserRegistrationDTO dto = registration("Ana", "ana@example.com", "secret", "client", "Rua Principal");
        when(userRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(client(2L, "other@example.com", "Other")));

        assertThatThrownBy(() -> userService.createUser(dto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Email already exists");
    }

    // Case: invalid registration (email already exists)
    @Test
    @DisplayName("Case: invalid registration (name is missing)")
    void invalidRegistration_nameMissing_shouldFail() {
        UserRegistrationDTO dto = registration(" ", "ana@example.com", "secret", "client", "Rua Principal");

        assertThatThrownBy(() -> userService.createUser(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Name is required");
    }

    @Test
    @DisplayName("Case: invalid registration (email is missing)")
    void invalidRegistration_emailMissing_shouldFail() {
        UserRegistrationDTO dto = registration("Ana", " ", "secret", "client", "Rua Principal");

        assertThatThrownBy(() -> userService.createUser(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email is required");
    }

    // Case: valid login (email exists)
    @Test
    @DisplayName("Case: invalid registration (password is missing)")
    void invalidRegistration_passwordMissing_shouldFail() {
        UserRegistrationDTO dto = registration("Ana", "ana@example.com", "", "client", "Rua Principal");

        assertThatThrownBy(() -> userService.createUser(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password is required");
    }

    // Case: invalid login (blank email)
    @Test
    @DisplayName("Case: invalid registration (type is missing)")
    void invalidRegistration_typeMissing_shouldFail() {
        UserRegistrationDTO dto = registration("Ana", "ana@example.com", "secret", "", "Rua Principal");

        assertThatThrownBy(() -> userService.createUser(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Type is required");
    }

    // Case: update user (user does not exist)
    @Test
    @DisplayName("Case: invalid registration (address is missing)")
    void invalidRegistration_addressMissing_shouldFailForClient() {
        UserRegistrationDTO dto = registration("Ana", "ana@example.com", "secret", "client", " ");

        assertThatThrownBy(() -> userService.createUser(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Address is required for client users");
    }

    // Case: delete user (user exists)
    @Test
    @DisplayName("Case: registration supports all user types")
    void registration_shouldSupportAllUserTypes() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User client = userService.createUser(registration("Ana", "ana@example.com", "secret", "client", "Rua A"));
        User owner = userService.createUser(registration("Boss", "boss@example.com", "secret", "restaurant owner", null));
        User driver = userService.createUser(registration("Dan", "dan@example.com", "secret", "driver", null));

        assertThat(client).isInstanceOf(Client.class);
        assertThat(owner).isInstanceOf(RestaurantAdmin.class);
        assertThat(driver).isInstanceOf(Driver.class);
    }

    @Test
    @DisplayName("Case: invalid login (email is missing)")
    void invalidLogin_emailMissing_shouldReturnFalse() {
        assertThat(userService.login(new UserLoginDTO("", "secret"))).isFalse();
    }

    @Test
    @DisplayName("Case: valid login (email and password are correct)")
    void validLogin_credentialsCorrect_shouldReturnTrue() {
        when(authService.login("ana@example.com")).thenReturn(client(1L, "ana@example.com", "Ana"));

        assertThat(userService.login(new UserLoginDTO("ana@example.com", "secret"))).isTrue();
    }

    @Test
    @DisplayName("Case: valid login but user does not exist")
    void validLoginButUserDoesNotExist_shouldReturnFalse() {
        when(authService.login("ghost@example.com")).thenReturn(null);

        assertThat(userService.login(new UserLoginDTO("ghost@example.com", "secret"))).isFalse();
    }

    @Test
    @DisplayName("Case: update user")
    void updateUser_shouldUpdateBasicFields() {
        Client existing = client(1L, "ana@example.com", "Ana");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = userService.updateUser(1L, update("Ana Updated", "ana@example.com", "newPass", "client", null, false));

        assertThat(updated.getName()).isEqualTo("Ana Updated");
        assertThat(updated.getPassword()).isEqualTo("newPass");
    }

    @Test
    @DisplayName("Case: get all users")
    void getAllUsers_shouldReturnRepositoryValues() {
        when(userRepository.findAll()).thenReturn(List.of(client(1L, "ana@example.com", "Ana"), client(2L, "bob@example.com", "Bob")));

        assertThat(userService.getAllUsers()).hasSize(2);
    }

    @Test
    @DisplayName("Case: delete user")
    void deleteUser_shouldReturnTrueWhenUserExists() {
        when(userRepository.existsById(10L)).thenReturn(true);

        assertThat(userService.deleteUser(10L)).isTrue();
        verify(userRepository).deleteById(10L);
    }

    // Case: get all users
    @Test
    @DisplayName("Case: get user by id")
    void getUserById_shouldReturnUserWhenExists() {
        Client expected = client(7L, "id@example.com", "ById");
        when(userRepository.findById(7L)).thenReturn(Optional.of(expected));

        assertThat(userService.getUserById(7L)).isSameAs(expected);
    }

    @Test
    @DisplayName("Case: get user by email")
    void getUserByEmail_shouldReturnUserWhenExists() {
        Client expected = client(8L, "mail@example.com", "ByEmail");
        when(userRepository.findByEmail("mail@example.com")).thenReturn(Optional.of(expected));

        assertThat(userService.getUserByEmail("mail@example.com")).isSameAs(expected);
    }

    @Test
    @DisplayName("Case: update user with email that already exists")
    void updateUser_emailAlreadyExists_shouldFail() {
        Client existing = client(1L, "ana@example.com", "Ana");
        Client other = client(2L, "taken@example.com", "Other");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.findByEmail("taken@example.com")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> userService.updateUser(1L, update("Ana", "taken@example.com", "secret", "client", null, false)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Email already exists");
    }

    @Test
    @DisplayName("Case: update user with email that does not exist")
    void updateUser_emailDoesNotExist_shouldUpdateEmail() {
        Client existing = client(1L, "ana@example.com", "Ana");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = userService.updateUser(1L, update("Ana", "new@example.com", "secret", "client", null, false));

        assertThat(updated.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    @DisplayName("Case: update user with a name that already exists")
    void updateUser_nameAlreadyExists_shouldStillUpdateName() {
        Client existing = client(1L, "ana@example.com", "Ana");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = userService.updateUser(1L, update("Bob", "ana@example.com", "secret", "client", null, false));

        assertThat(updated.getName()).isEqualTo("Bob");
    }

    @Test
    @DisplayName("Case: update user with a name that does not exist")
    void updateUser_nameDoesNotExist_shouldUpdateName() {
        Client existing = client(1L, "ana@example.com", "Ana");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = userService.updateUser(1L, update("Carla", "ana@example.com", "secret", "client", null, false));

        assertThat(updated.getName()).isEqualTo("Carla");
    }

    @Test
    @DisplayName("Case: update user with a password that does not match")
    void updateUser_passwordDoesNotMatch_shouldUpdatePassword() {
        Client existing = client(1L, "ana@example.com", "Ana");
        existing.setPassword("old");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = userService.updateUser(1L, update("Ana", "ana@example.com", "different", "client", null, false));

        assertThat(updated.getPassword()).isEqualTo("different");
    }

    @Test
    @DisplayName("Case: update user with a password that matches")
    void updateUser_passwordMatches_shouldKeepPasswordValue() {
        Client existing = client(1L, "ana@example.com", "Ana");
        existing.setPassword("same");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = userService.updateUser(1L, update("Ana", "ana@example.com", "same", "client", null, false));

        assertThat(updated.getPassword()).isEqualTo("same");
    }

    @Test
    @DisplayName("Case: update user with a type that does not exist")
    void updateUser_typeDoesNotExist_shouldIgnoreTypeAndPersist() {
        Client existing = client(1L, "ana@example.com", "Ana");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = userService.updateUser(1L, update("Ana", "ana@example.com", "secret", "alien", null, false));

        assertThat(updated).isInstanceOf(Client.class);
    }

    @Test
    @DisplayName("Case: update user with a type that exists")
    void updateUser_typeExists_shouldIgnoreTypeAndPersist() {
        Client existing = client(1L, "ana@example.com", "Ana");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = userService.updateUser(1L, update("Ana", "ana@example.com", "secret", "driver", null, false));

        assertThat(updated).isInstanceOf(Client.class);
    }

    @Test
    @DisplayName("Case: update user with an address that does not exist")
    void updateUser_addressDoesNotExist_shouldKeepAddressNull() {
        Client existing = client(1L, "ana@example.com", "Ana");
        existing.setAddress(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = userService.updateUser(1L, update("Ana", "ana@example.com", "secret", "client", "Rua Nova", false));

        assertThat(((Client) updated).getAddress()).isNull();
    }

    @Test
    @DisplayName("Case: update user with an address that exists")
    void updateUser_addressExists_shouldUpdateAddressStreet() {
        Client existing = clientWithAddress(1L, "ana@example.com", "Ana", "Rua Velha");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = userService.updateUser(1L, update("Ana", "ana@example.com", "secret", "client", "Rua Nova", false));

        assertThat(((Client) updated).getAddress().getStreet()).isEqualTo("Rua Nova");
    }

    @Test
    @DisplayName("Case: update user with an address that is null")
    void updateUser_addressIsNull_shouldNotChangeExistingAddress() {
        Client existing = clientWithAddress(1L, "ana@example.com", "Ana", "Rua Velha");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = userService.updateUser(1L, update("Ana", "ana@example.com", "secret", "client", null, false));

        assertThat(((Client) updated).getAddress().getStreet()).isEqualTo("Rua Velha");
    }

    @Test
    @DisplayName("Case: update user with an address that is not null")
    void updateUser_addressIsNotNull_shouldChangeExistingAddress() {
        Client existing = clientWithAddress(1L, "ana@example.com", "Ana", "Rua Antiga");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = userService.updateUser(1L, update("Ana", "ana@example.com", "secret", "client", "Rua Atualizada", false));

        assertThat(((Client) updated).getAddress().getStreet()).isEqualTo("Rua Atualizada");
    }

    private UserRegistrationDTO registration(String name, String email, String password, String type, String address) {
        return new UserRegistrationDTO(name, email, password, type, address);
    }

    private UserUpdateDTO update(String name, String email, String password, String type, String address, boolean available) {
        return new UserUpdateDTO(name, email, password, type, address, available);
    }

    private Client client(long id, String email, String name) {
        Client client = new Client();
        client.setId(id);
        client.setEmail(email);
        client.setName(name);
        client.setPassword("secret");
        return client;
    }

    private Client clientWithAddress(long id, String email, String name, String street) {
        Client client = client(id, email, name);
        Address address = new Address();
        address.setStreet(street);
        client.setAddress(address);
        return client;
    }
}


