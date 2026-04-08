package pt.ul.fc.css.tascaeats.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.ul.fc.css.tascaeats.domain.user.*;
import pt.ul.fc.css.tascaeats.dto.AddressDTO;
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

    // Registration
    @Test
    @DisplayName("Case: valid registration creates client with address")
    void shouldCreateClientWithAddressWhenRegistrationIsValid() {
        UserRegistrationDTO dto = registration("Ana", "ana@example.com", "secret", "client", address("Rua Principal"));
        when(userRepository.findByEmail("ana@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User created = userService.createUser(dto);

        assertThat(created).isInstanceOf(Client.class);
        assertThat(created.getEmail()).isEqualTo("ana@example.com");
    }

    @Test
    @DisplayName("Case: invalid registration fails when email already exists")
    void shouldFailRegistrationWhenEmailAlreadyExists() {
        UserRegistrationDTO dto = registration("Ana", "ana@example.com", "secret", "client", address("Rua Principal"));
        when(userRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(client(2L, "other@example.com", "Other")));

        assertThatThrownBy(() -> userService.createUser(dto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Email already exists");
    }

    @Test
    @DisplayName("Case: invalid registration fails when name is missing")
    void shouldFailRegistrationWhenNameIsMissing() {
        UserRegistrationDTO dto = registration(" ", "ana@example.com", "secret", "client", address("Rua Principal"));

        assertThatThrownBy(() -> userService.createUser(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Name is required");
    }

    @Test
    @DisplayName("Case: invalid registration fails when email is missing")
    void shouldFailRegistrationWhenEmailIsMissing() {
        UserRegistrationDTO dto = registration("Ana", " ", "secret", "client", address("Rua Principal"));

        assertThatThrownBy(() -> userService.createUser(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email is required");
    }

    @Test
    @DisplayName("Case: invalid registration fails when password is missing")
    void shouldFailRegistrationWhenPasswordIsMissing() {
        UserRegistrationDTO dto = registration("Ana", "ana@example.com", "", "client", address("Rua Principal"));

        assertThatThrownBy(() -> userService.createUser(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password is required");
    }

    @Test
    @DisplayName("Case: invalid registration fails when type is missing")
    void shouldFailRegistrationWhenTypeIsMissing() {
        UserRegistrationDTO dto = registration("Ana", "ana@example.com", "secret", "", address("Rua Principal"));

        assertThatThrownBy(() -> userService.createUser(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Type is required");
    }

    @Test
    @DisplayName("Case: invalid registration fails when client address is missing")
    void shouldFailRegistrationWhenClientAddressIsMissing() {
        UserRegistrationDTO dto = registration("Ana", "ana@example.com", "secret", "client", null);

        assertThatThrownBy(() -> userService.createUser(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Address is required for client users");
    }

    @Test
    @DisplayName("Case: valid registration supports all user types")
    void shouldSupportAllUserTypesWhenRegistrationIsValid() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User client = userService.createUser(registration("Ana", "ana@example.com", "secret", "client", address("Rua A")));
        User owner = userService.createUser(registration("Boss", "boss@example.com", "secret", "restaurant owner", null));
        User driver = userService.createUser(registration("Dan", "dan@example.com", "secret", "driver", null));

        assertThat(client).isInstanceOf(Client.class);
        assertThat(owner).isInstanceOf(RestaurantAdmin.class);
        assertThat(driver).isInstanceOf(Driver.class);
    }

    // Login
    @Test
    @DisplayName("Case: invalid login returns false when email is missing")
    void shouldReturnFalseWhenLoginEmailIsMissing() {
        assertThat(userService.login(new UserLoginDTO("", "secret"))).isFalse();
    }

    @Test
    @DisplayName("Case: valid login returns true when user exists")
    void shouldReturnTrueWhenLoginUserExists() {
        when(authService.login("ana@example.com")).thenReturn(client(1L, "ana@example.com", "Ana"));

        assertThat(userService.login(new UserLoginDTO("ana@example.com", "secret"))).isTrue();
    }

    @Test
    @DisplayName("Case: valid login returns false when user does not exist")
    void shouldReturnFalseWhenLoginUserDoesNotExist() {
        when(authService.login("ghost@example.com")).thenReturn(null);

        assertThat(userService.login(new UserLoginDTO("ghost@example.com", "secret"))).isFalse();
    }

    // Manage users
    @Test
    @DisplayName("Case: manage users update user updates basic fields")
    void shouldUpdateBasicFieldsWhenUpdatingUser() {
        Client existing = client(1L, "ana@example.com", "Ana");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = userService.updateUser(1L, update("Ana Updated", "ana@example.com", "newPass", "client", null, false));

        assertThat(updated.getName()).isEqualTo("Ana Updated");
        assertThat(updated.getPassword()).isEqualTo("newPass");
    }

    @Test
    @DisplayName("Case: manage users get all users returns repository values")
    void shouldReturnRepositoryValuesWhenGettingAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(client(1L, "ana@example.com", "Ana"), client(2L, "bob@example.com", "Bob")));

        assertThat(userService.getAllUsers()).hasSize(2);
    }

    @Test
    @DisplayName("Case: manage users delete user returns true when user exists")
    void shouldReturnTrueWhenDeletingExistingUser() {
        when(userRepository.existsById(10L)).thenReturn(true);

        assertThat(userService.deleteUser(10L)).isTrue();
        verify(userRepository).deleteById(10L);
    }

    @Test
    @DisplayName("Case: manage users get user by id returns user when exists")
    void shouldReturnUserWhenGettingUserByIdAndItExists() {
        Client expected = client(7L, "id@example.com", "ById");
        when(userRepository.findById(7L)).thenReturn(Optional.of(expected));

        assertThat(userService.getUserById(7L)).isSameAs(expected);
    }

    @Test
    @DisplayName("Case: manage users get user by email returns user when exists")
    void shouldReturnUserWhenGettingUserByEmailAndItExists() {
        Client expected = client(8L, "mail@example.com", "ByEmail");
        when(userRepository.findByEmail("mail@example.com")).thenReturn(Optional.of(expected));

        assertThat(userService.getUserByEmail("mail@example.com")).isSameAs(expected);
    }

    @Test
    @DisplayName("Case: manage users update user fails when email already exists")
    void shouldFailUpdateWhenEmailAlreadyExists() {
        Client existing = client(1L, "ana@example.com", "Ana");
        Client other = client(2L, "taken@example.com", "Other");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.findByEmail("taken@example.com")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> userService.updateUser(1L, update("Ana", "taken@example.com", "secret", "client", null, false)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Email already exists");
    }

    @Test
    @DisplayName("Case: manage users update user changes email when email is available")
    void shouldUpdateEmailWhenUpdateEmailIsAvailable() {
        Client existing = client(1L, "ana@example.com", "Ana");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = userService.updateUser(1L, update("Ana", "new@example.com", "secret", "client", null, false));

        assertThat(updated.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    @DisplayName("Case: manage users update user accepts name even if repeated")
    void shouldAcceptRepeatedNameWhenUpdatingUser() {
        Client existing = client(1L, "ana@example.com", "Ana");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = userService.updateUser(1L, update("Bob", "ana@example.com", "secret", "client", null, false));

        assertThat(updated.getName()).isEqualTo("Bob");
    }

    @Test
    @DisplayName("Case: manage users update user changes name when provided")
    void shouldUpdateNameWhenNameIsProvided() {
        Client existing = client(1L, "ana@example.com", "Ana");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = userService.updateUser(1L, update("Carla", "ana@example.com", "secret", "client", null, false));

        assertThat(updated.getName()).isEqualTo("Carla");
    }

    @Test
    @DisplayName("Case: manage users update user changes password when different")
    void shouldUpdatePasswordWhenPasswordIsDifferent() {
        Client existing = client(1L, "ana@example.com", "Ana");
        existing.setPassword("old");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = userService.updateUser(1L, update("Ana", "ana@example.com", "different", "client", null, false));

        assertThat(updated.getPassword()).isEqualTo("different");
    }

    @Test
    @DisplayName("Case: manage users update user keeps password when same")
    void shouldKeepPasswordWhenPasswordIsSame() {
        Client existing = client(1L, "ana@example.com", "Ana");
        existing.setPassword("same");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = userService.updateUser(1L, update("Ana", "ana@example.com", "same", "client", null, false));

        assertThat(updated.getPassword()).isEqualTo("same");
    }

    @Test
    @DisplayName("Case: manage users update user ignores unknown type")
    void shouldIgnoreUnknownTypeWhenUpdatingUser() {
        Client existing = client(1L, "ana@example.com", "Ana");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = userService.updateUser(1L, update("Ana", "ana@example.com", "secret", "alien", null, false));

        assertThat(updated).isInstanceOf(Client.class);
    }

    @Test
    @DisplayName("Case: manage users update user does not change class by type field")
    void shouldNotChangeClassWhenUpdatingTypeField() {
        Client existing = client(1L, "ana@example.com", "Ana");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = userService.updateUser(1L, update("Ana", "ana@example.com", "secret", "driver", null, false));

        assertThat(updated).isInstanceOf(Client.class);
    }

    @Test
    @DisplayName("Case: manage users update user keeps address null when client has no address")
    void shouldKeepAddressNullWhenClientHasNoAddress() {
        Client existing = client(1L, "ana@example.com", "Ana");
        existing.setAddress(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = userService.updateUser(1L, update("Ana", "ana@example.com", "secret", "client", address("Rua Nova"), false));

        assertThat(((Client) updated).getAddress()).isNull();
    }

    @Test
    @DisplayName("Case: manage users update user changes address when address exists")
    void shouldUpdateAddressWhenClientAlreadyHasAddress() {
        Client existing = clientWithAddress(1L, "ana@example.com", "Ana", "Rua Velha");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = userService.updateUser(1L, update("Ana", "ana@example.com", "secret", "client", address("Rua Nova"), false));

        assertThat(((Client) updated).getAddress().getStreet()).isEqualTo("Rua Nova");
    }

    @Test
    @DisplayName("Case: manage users update user does not change address when dto address is null")
    void shouldNotChangeAddressWhenDtoAddressIsNull() {
        Client existing = clientWithAddress(1L, "ana@example.com", "Ana", "Rua Velha");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = userService.updateUser(1L, update("Ana", "ana@example.com", "secret", "client", null, false));

        assertThat(((Client) updated).getAddress().getStreet()).isEqualTo("Rua Velha");
    }

    @Test
    @DisplayName("Case: manage users update user changes address when dto address is not null")
    void shouldChangeAddressWhenDtoAddressIsNotNull() {
        Client existing = clientWithAddress(1L, "ana@example.com", "Ana", "Rua Antiga");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = userService.updateUser(1L, update("Ana", "ana@example.com", "secret", "client", address("Rua Atualizada"), false));

        assertThat(((Client) updated).getAddress().getStreet()).isEqualTo("Rua Atualizada");
    }

    private UserRegistrationDTO registration(String name, String email, String password, String type, AddressDTO address) {
        return new UserRegistrationDTO(name, email, password, type, address);
    }

    private UserUpdateDTO update(String name, String email, String password, String type, AddressDTO address, boolean available) {
        return new UserUpdateDTO(name, email, password, type, address, available);
    }

    private AddressDTO address(String street) {
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setStreet(street);
        addressDTO.setCountry("Portugal");
        addressDTO.setDistrict("Lisboa");
        addressDTO.setCity("Lisboa");
        addressDTO.setPostalCode("1000-001");
        return addressDTO;
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
        address.setCountry("Portugal");
        address.setDistrict("Lisboa");
        address.setCity("Lisboa");
        address.setPostalCode("1000-001");
        client.setAddress(address);
        return client;
    }
}


