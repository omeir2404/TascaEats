package pt.ul.fc.css.tascaeats.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;
import pt.ul.fc.css.tascaeats.domain.user.Address;
import pt.ul.fc.css.tascaeats.domain.user.Client;
import pt.ul.fc.css.tascaeats.domain.user.Driver;
import pt.ul.fc.css.tascaeats.domain.user.User;
import pt.ul.fc.css.tascaeats.dto.*;
import pt.ul.fc.css.tascaeats.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private static final String USER_NOT_FOUND = "User not found";

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegistrationDTO userRegistrationDTO) {
        try {
            User created = userService.createUser(userRegistrationDTO);
            UserRegistrationResponseDTO response = new UserRegistrationResponseDTO(
                    "User registered successfully",
                    created.getId(),
                    // Escape user-provided fields before returning them in the response body.
                    sanitizeForOutput(created.getEmail()),
                    created.getClass().getSimpleName()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(error("Invalid registration data"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error("User already exists"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiMessageDTO> login(@RequestBody UserLoginDTO userLoginDTO) {
        boolean ok = userService.login(userLoginDTO);
        if (!ok) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiMessageDTO("Invalid user"));
        }
        return ResponseEntity.ok(new ApiMessageDTO("Login successful (mock)"));
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponseDTO>> getUsers() {
        List<UserResponseDTO> users = userService.getAllUsers()
                .stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<Object> getUserById(@PathVariable("id") Long id) {
        User user = userService.getUserById(id);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(USER_NOT_FOUND));
        }
        return ResponseEntity.ok(toUserResponse(user));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<Object> updateUser(@PathVariable("id") Long id,
                                             @RequestBody UserUpdateDTO dto) {
        try {
            if (dto.getType() != null && dto.getType().equalsIgnoreCase("driver")) {
                return respondWithUpdatedUser(userService.updateDriver(id, dto));
            }
            return respondWithUpdatedUser(userService.updateUser(id, dto));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error("User update conflict"));
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Object> deleteUser(@PathVariable("id") Long id) {
        boolean deleted = userService.deleteUser(id);
        if (!deleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(USER_NOT_FOUND));
        }
        return ResponseEntity.noContent().build();
    }

    private String sanitizeForOutput(String value) {
        return value == null ? null : HtmlUtils.htmlEscape(value);
    }

    private ResponseEntity<Object> respondWithUpdatedUser(User updated) {
        if (updated == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(USER_NOT_FOUND));
        }
        return ResponseEntity.ok(toUserResponse(updated));
    }

    private ApiErrorDTO error(String message) {
        return new ApiErrorDTO(message);
    }

    private UserResponseDTO toUserResponse(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setType(user.getClass().getSimpleName());

        if (user instanceof Driver driver) {
            dto.setAvailable(driver.isAvailable());
        }
        if (user instanceof Client client) {
            dto.setAddress(toAddressResponse(client.getAddress()));
        }

        return dto;
    }

    private AddressResponseDTO toAddressResponse(Address address) {
        if (address == null) {
            return null;
        }
        return new AddressResponseDTO(
                address.getStreet(),
                address.getNumber(),
                address.getComplement(),
                address.getCountry(),
                address.getDistrict(),
                address.getCity(),
                address.getPostalCode()
        );
    }
}
