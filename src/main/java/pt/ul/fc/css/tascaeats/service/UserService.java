package pt.ul.fc.css.tascaeats.service;

import org.springframework.stereotype.Service;
import pt.ul.fc.css.tascaeats.domain.user.*;
import pt.ul.fc.css.tascaeats.dto.UserLoginDTO;
import pt.ul.fc.css.tascaeats.dto.UserRegistrationDTO;
import pt.ul.fc.css.tascaeats.dto.UserUpdateDTO;
import pt.ul.fc.css.tascaeats.repository.UserRepository;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final AuthService authService;

    public UserService(UserRepository userRepository, AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    public User createUser(UserRegistrationDTO dto) {
        validateRegistration(dto);

        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalStateException("Email already exists");
        }

        User user = buildUserFromType(dto.getType());
        applyCommonFields(user, dto);
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public User getUserByEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return userRepository.findByEmail(email).orElse(null);
    }

    public User updateDriver(Long id, UserUpdateDTO dto) {
        User existing = userRepository.findById(id).orElse(null);
        if (existing == null) {
            return null;
        }
        // updated availability before using geral updateUser
        if (existing instanceof Driver driver) {
            driver.setAvailable(dto.isAvailable());
        }
        return updateUser(id, dto);
    }

    public User updateUser(Long id, UserUpdateDTO dto) {
        User existing = userRepository.findById(id).orElse(null);
        if (existing == null) {
            return null;
        }

        if (dto.getEmail() != null && !dto.getEmail().equals(existing.getEmail())) {
            Optional<User> byEmail = userRepository.findByEmail(dto.getEmail());
            if (byEmail.isPresent() && byEmail.get().getId() != existing.getId()) {
                throw new IllegalStateException("Email already exists");
            }
            existing.setEmail(dto.getEmail());
        }

        if (dto.getName() != null) {
            existing.setName(dto.getName());
        }
        if (dto.getPassword() != null) {
            existing.setPassword(dto.getPassword());
        }

        if (existing instanceof Client client && dto.getAddress() != null && client.getAddress() != null) {
            // Keep Address update minimal here; creating Address requires cleaner mapping in the domain.
            client.getAddress().setStreet(dto.getAddress());
        }

        return userRepository.save(existing);
    }

    public boolean deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            return false;
        }
        userRepository.deleteById(id);
        return true;
    }

    public boolean login(UserLoginDTO userLoginDTO) {
        if (userLoginDTO == null || userLoginDTO.getEmail() == null || userLoginDTO.getEmail().isBlank()) {
            return false;
        }
        // Mock login: any password is accepted as long as the user exists.
        return authService.login(userLoginDTO.getEmail()) != null;
    }

    private void validateRegistration(UserRegistrationDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (dto.getType() == null || dto.getType().isBlank()) {
            throw new IllegalArgumentException("Type is required");
        }
        if ("client".equalsIgnoreCase(dto.getType()) && (dto.getAddress() == null || dto.getAddress().isBlank())) {
            throw new IllegalArgumentException("Address is required for client users");
        }
    }

    private User buildUserFromType(String type) {
        String normalized = type.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "client" -> new Client();
            case "driver" -> new Driver();
            case "restaurantadmin", "restaurant_admin", "restaurant owner", "restaurantowner", "admin" -> new RestaurantAdmin();
            default -> throw new IllegalArgumentException("Invalid user type: " + type);
        };
    }

    private void applyCommonFields(User user, UserRegistrationDTO dto) {
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());

        if (user instanceof Client client && dto.getAddress() != null && !dto.getAddress().isBlank()) {
            Address address = new Address();
            address.setStreet(dto.getAddress());
            client.setAddress(address);
        }
    }
}
