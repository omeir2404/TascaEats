package pt.ul.fc.css.tascaeats.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.ul.fc.css.tascaeats.domain.Utilizador.*;
import pt.ul.fc.css.tascaeats.dto.MoradaDTO;
import pt.ul.fc.css.tascaeats.dto.UtilizadorLoginDTO;
import pt.ul.fc.css.tascaeats.dto.UtilizadorRegistoDTO;
import pt.ul.fc.css.tascaeats.dto.UtilizadorUpdateDTO;
import pt.ul.fc.css.tascaeats.repository.UtilizadorRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UtilizadorServiceTest {

    @Mock
    private UtilizadorRepository UtilizadorRepository;

    @Mock
    private AutorService authService;

    @InjectMocks
    private UtilizadorService UtilizadorService;

    // Registration
    @Test
    @DisplayName("Case: valid registration creates client with address")
    void shouldCreateClienteWithMoradaWhenRegistrationIsValid() {
        UtilizadorRegistoDTO dto = registration("Ana", "ana@example.com", "secret", "client", address("Rua Principal"));
        when(UtilizadorRepository.findByEmail("ana@example.com")).thenReturn(Optional.empty());
        when(UtilizadorRepository.save(any(Utilizador.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Utilizador created = UtilizadorService.createUtilizador(dto);

        assertThat(created).isInstanceOf(Cliente.class);
        assertThat(created.getEmail()).isEqualTo("ana@example.com");
    }

    @Test
    @DisplayName("Case: invalid registration fails when email already exists")
    void shouldFailRegistrationWhenEmailAlreadyExists() {
        UtilizadorRegistoDTO dto = registration("Ana", "ana@example.com", "secret", "client", address("Rua Principal"));
        when(UtilizadorRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(client(2L, "other@example.com", "Other")));

        assertThatThrownBy(() -> UtilizadorService.createUtilizador(dto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Email already exists");
    }

    @Test
    @DisplayName("Case: invalid registration fails when name is missing")
    void shouldFailRegistrationWhenNameIsMissing() {
        UtilizadorRegistoDTO dto = registration(" ", "ana@example.com", "secret", "client", address("Rua Principal"));

        assertThatThrownBy(() -> UtilizadorService.createUtilizador(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Name is required");
    }

    @Test
    @DisplayName("Case: invalid registration fails when email is missing")
    void shouldFailRegistrationWhenEmailIsMissing() {
        UtilizadorRegistoDTO dto = registration("Ana", " ", "secret", "client", address("Rua Principal"));

        assertThatThrownBy(() -> UtilizadorService.createUtilizador(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email is required");
    }

    @Test
    @DisplayName("Case: invalid registration fails when password is missing")
    void shouldFailRegistrationWhenPasswordIsMissing() {
        UtilizadorRegistoDTO dto = registration("Ana", "ana@example.com", "", "client", address("Rua Principal"));

        assertThatThrownBy(() -> UtilizadorService.createUtilizador(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password is required");
    }

    @Test
    @DisplayName("Case: invalid registration fails when type is missing")
    void shouldFailRegistrationWhenTypeIsMissing() {
        UtilizadorRegistoDTO dto = registration("Ana", "ana@example.com", "secret", "", address("Rua Principal"));

        assertThatThrownBy(() -> UtilizadorService.createUtilizador(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Type is required");
    }

    @Test
    @DisplayName("Case: invalid registration fails when client address is missing")
    void shouldFailRegistrationWhenClienteMoradaIsMissing() {
        UtilizadorRegistoDTO dto = registration("Ana", "ana@example.com", "secret", "client", null);

        assertThatThrownBy(() -> UtilizadorService.createUtilizador(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Morada is required for client Utilizadors");
    }

    @Test
    @DisplayName("Case: valid registration supports all Utilizador types")
    void shouldSupportAllUtilizadorTypesWhenRegistrationIsValid() {
        when(UtilizadorRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(UtilizadorRepository.save(any(Utilizador.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Utilizador client = UtilizadorService.createUtilizador(registration("Ana", "ana@example.com", "secret", "client", address("Rua A")));
        Utilizador owner = UtilizadorService.createUtilizador(registration("Boss", "boss@example.com", "secret", "restaurant owner", null));
        Utilizador driver = UtilizadorService.createUtilizador(registration("Dan", "dan@example.com", "secret", "driver", null));

        assertThat(client).isInstanceOf(Cliente.class);
        assertThat(owner).isInstanceOf(RestauranteAdmin.class);
        assertThat(driver).isInstanceOf(Entregador.class);
    }

    // Login
    @Test
    @DisplayName("Case: invalid login returns false when email is missing")
    void shouldReturnFalseWhenLoginEmailIsMissing() {
        assertThat(UtilizadorService.login(new UtilizadorLoginDTO("", "secret"))).isFalse();
    }

    @Test
    @DisplayName("Case: valid login returns true when Utilizador exists")
    void shouldReturnTrueWhenLoginUtilizadorExists() {
        when(authService.login("ana@example.com")).thenReturn(client(1L, "ana@example.com", "Ana"));

        assertThat(UtilizadorService.login(new UtilizadorLoginDTO("ana@example.com", "secret"))).isTrue();
    }

    @Test
    @DisplayName("Case: valid login returns false when Utilizador does not exist")
    void shouldReturnFalseWhenLoginUtilizadorDoesNotExist() {
        when(authService.login("ghost@example.com")).thenReturn(null);

        assertThat(UtilizadorService.login(new UtilizadorLoginDTO("ghost@example.com", "secret"))).isFalse();
    }

    // Manage Utilizadors
    @Test
    @DisplayName("Case: manage Utilizadors update Utilizador updates basic fields")
    void shouldUpdateBasicFieldsWhenUpdatingUtilizador() {
        Cliente existing = client(1L, "ana@example.com", "Ana");
        when(UtilizadorRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(UtilizadorRepository.save(any(Utilizador.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Utilizador updated = UtilizadorService.updateUtilizador(1L, update("Ana Updated", "ana@example.com", "newPass", "client", null, false));

        assertThat(updated.getName()).isEqualTo("Ana Updated");
        assertThat(updated.getPassword()).isEqualTo("newPass");
    }

    @Test
    @DisplayName("Case: manage Utilizadors get all Utilizadors returns repository values")
    void shouldReturnRepositoryValuesWhenGettingAllUtilizadors() {
        when(UtilizadorRepository.findAll()).thenReturn(List.of(client(1L, "ana@example.com", "Ana"), client(2L, "bob@example.com", "Bob")));

        assertThat(UtilizadorService.getAllUtilizadors()).hasSize(2);
    }

    @Test
    @DisplayName("Case: manage Utilizadors delete Utilizador returns true when Utilizador exists")
    void shouldReturnTrueWhenDeletingExistingUtilizador() {
        when(UtilizadorRepository.existsById(10L)).thenReturn(true);

        assertThat(UtilizadorService.deleteUtilizador(10L)).isTrue();
        verify(UtilizadorRepository).deleteById(10L);
    }

    @Test
    @DisplayName("Case: manage Utilizadors get Utilizador by id returns Utilizador when exists")
    void shouldReturnUtilizadorWhenGettingUtilizadorByIdAndItExists() {
        Cliente expected = client(7L, "id@example.com", "ById");
        when(UtilizadorRepository.findById(7L)).thenReturn(Optional.of(expected));

        assertThat(UtilizadorService.getUtilizadorById(7L)).isSameAs(expected);
    }

    @Test
    @DisplayName("Case: manage Utilizadors get Utilizador by email returns Utilizador when exists")
    void shouldReturnUtilizadorWhenGettingUtilizadorByEmailAndItExists() {
        Cliente expected = client(8L, "mail@example.com", "ByEmail");
        when(UtilizadorRepository.findByEmail("mail@example.com")).thenReturn(Optional.of(expected));

        assertThat(UtilizadorService.getUtilizadorByEmail("mail@example.com")).isSameAs(expected);
    }

    @Test
    @DisplayName("Case: manage Utilizadors update Utilizador fails when email already exists")
    void shouldFailUpdateWhenEmailAlreadyExists() {
        Cliente existing = client(1L, "ana@example.com", "Ana");
        Cliente other = client(2L, "taken@example.com", "Other");
        when(UtilizadorRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(UtilizadorRepository.findByEmail("taken@example.com")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> UtilizadorService.updateUtilizador(1L, update("Ana", "taken@example.com", "secret", "client", null, false)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Email already exists");
    }

    @Test
    @DisplayName("Case: manage Utilizadors update Utilizador changes email when email is available")
    void shouldUpdateEmailWhenUpdateEmailIsAvailable() {
        Cliente existing = client(1L, "ana@example.com", "Ana");
        when(UtilizadorRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(UtilizadorRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(UtilizadorRepository.save(any(Utilizador.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Utilizador updated = UtilizadorService.updateUtilizador(1L, update("Ana", "new@example.com", "secret", "client", null, false));

        assertThat(updated.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    @DisplayName("Case: manage Utilizadors update Utilizador accepts name even if repeated")
    void shouldAcceptRepeatedNameWhenUpdatingUtilizador() {
        Cliente existing = client(1L, "ana@example.com", "Ana");
        when(UtilizadorRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(UtilizadorRepository.save(any(Utilizador.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Utilizador updated = UtilizadorService.updateUtilizador(1L, update("Bob", "ana@example.com", "secret", "client", null, false));

        assertThat(updated.getName()).isEqualTo("Bob");
    }

    @Test
    @DisplayName("Case: manage Utilizadors update Utilizador changes name when provided")
    void shouldUpdateNameWhenNameIsProvided() {
        Cliente existing = client(1L, "ana@example.com", "Ana");
        when(UtilizadorRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(UtilizadorRepository.save(any(Utilizador.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Utilizador updated = UtilizadorService.updateUtilizador(1L, update("Carla", "ana@example.com", "secret", "client", null, false));

        assertThat(updated.getName()).isEqualTo("Carla");
    }

    @Test
    @DisplayName("Case: manage Utilizadors update Utilizador changes password when different")
    void shouldUpdatePasswordWhenPasswordIsDifferent() {
        Cliente existing = client(1L, "ana@example.com", "Ana");
        existing.setPassword("old");
        when(UtilizadorRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(UtilizadorRepository.save(any(Utilizador.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Utilizador updated = UtilizadorService.updateUtilizador(1L, update("Ana", "ana@example.com", "different", "client", null, false));

        assertThat(updated.getPassword()).isEqualTo("different");
    }

    @Test
    @DisplayName("Case: manage Utilizadors update Utilizador keeps password when same")
    void shouldKeepPasswordWhenPasswordIsSame() {
        Cliente existing = client(1L, "ana@example.com", "Ana");
        existing.setPassword("same");
        when(UtilizadorRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(UtilizadorRepository.save(any(Utilizador.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Utilizador updated = UtilizadorService.updateUtilizador(1L, update("Ana", "ana@example.com", "same", "client", null, false));

        assertThat(updated.getPassword()).isEqualTo("same");
    }

    @Test
    @DisplayName("Case: manage Utilizadors update Utilizador ignores unknown type")
    void shouldIgnoreUnknownTypeWhenUpdatingUtilizador() {
        Cliente existing = client(1L, "ana@example.com", "Ana");
        when(UtilizadorRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(UtilizadorRepository.save(any(Utilizador.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Utilizador updated = UtilizadorService.updateUtilizador(1L, update("Ana", "ana@example.com", "secret", "alien", null, false));

        assertThat(updated).isInstanceOf(Cliente.class);
    }

    @Test
    @DisplayName("Case: manage Utilizadors update Utilizador does not change class by type field")
    void shouldNotChangeClassWhenUpdatingTypeField() {
        Cliente existing = client(1L, "ana@example.com", "Ana");
        when(UtilizadorRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(UtilizadorRepository.save(any(Utilizador.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Utilizador updated = UtilizadorService.updateUtilizador(1L, update("Ana", "ana@example.com", "secret", "driver", null, false));

        assertThat(updated).isInstanceOf(Cliente.class);
    }

    @Test
    @DisplayName("Case: manage Utilizadors update Utilizador keeps address null when client has no address")
    void shouldKeepMoradaNullWhenClienteHasNoMorada() {
        Cliente existing = client(1L, "ana@example.com", "Ana");
        existing.setMorada(null);
        when(UtilizadorRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(UtilizadorRepository.save(any(Utilizador.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Utilizador updated = UtilizadorService.updateUtilizador(1L, update("Ana", "ana@example.com", "secret", "client", address("Rua Nova"), false));

        assertThat(((Cliente) updated).getMorada()).isNull();
    }

    @Test
    @DisplayName("Case: manage Utilizadors update Utilizador changes address when address exists")
    void shouldUpdateMoradaWhenClienteAlreadyHasMorada() {
        Cliente existing = clientWithMorada(1L, "ana@example.com", "Ana", "Rua Velha");
        when(UtilizadorRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(UtilizadorRepository.save(any(Utilizador.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Utilizador updated = UtilizadorService.updateUtilizador(1L, update("Ana", "ana@example.com", "secret", "client", address("Rua Nova"), false));

        assertThat(((Cliente) updated).getMorada().getStreet()).isEqualTo("Rua Nova");
    }

    @Test
    @DisplayName("Case: manage Utilizadors update Utilizador does not change address when dto address is null")
    void shouldNotChangeMoradaWhenDtoMoradaIsNull() {
        Cliente existing = clientWithMorada(1L, "ana@example.com", "Ana", "Rua Velha");
        when(UtilizadorRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(UtilizadorRepository.save(any(Utilizador.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Utilizador updated = UtilizadorService.updateUtilizador(1L, update("Ana", "ana@example.com", "secret", "client", null, false));

        assertThat(((Cliente) updated).getMorada().getStreet()).isEqualTo("Rua Velha");
    }

    @Test
    @DisplayName("Case: manage Utilizadors update Utilizador changes address when dto address is not null")
    void shouldChangeMoradaWhenDtoMoradaIsNotNull() {
        Cliente existing = clientWithMorada(1L, "ana@example.com", "Ana", "Rua Antiga");
        when(UtilizadorRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(UtilizadorRepository.save(any(Utilizador.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Utilizador updated = UtilizadorService.updateUtilizador(1L, update("Ana", "ana@example.com", "secret", "client", address("Rua Atualizada"), false));

        assertThat(((Cliente) updated).getMorada().getStreet()).isEqualTo("Rua Atualizada");
    }

    private UtilizadorRegistoDTO registration(String name, String email, String password, String type, MoradaDTO address) {
        return new UtilizadorRegistoDTO(name, email, password, type, address);
    }

    private UtilizadorUpdateDTO update(String name, String email, String password, String type, MoradaDTO address, boolean available) {
        return new UtilizadorUpdateDTO(name, email, password, type, address, available);
    }

    private MoradaDTO address(String street) {
        MoradaDTO addressDTO = new MoradaDTO();
        addressDTO.setStreet(street);
        addressDTO.setCountry("Portugal");
        addressDTO.setDistrict("Lisboa");
        addressDTO.setCity("Lisboa");
        addressDTO.setPostalCode("1000-001");
        return addressDTO;
    }

    private Cliente client(long id, String email, String name) {
        Cliente client = new Cliente();
        client.setId(id);
        client.setEmail(email);
        client.setName(name);
        client.setPassword("secret");
        return client;
    }

    private Cliente clientWithMorada(long id, String email, String name, String street) {
        Cliente client = client(id, email, name);
        Morada address = new Morada();
        address.setStreet(street);
        address.setCountry("Portugal");
        address.setDistrict("Lisboa");
        address.setCity("Lisboa");
        address.setPostalCode("1000-001");
        client.setMorada(address);
        return client;
    }
}


