package pt.ul.fc.css.tascaeats.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;
import pt.ul.fc.css.tascaeats.domain.user.User;
import pt.ul.fc.css.tascaeats.dto.UserLoginDTO;
import pt.ul.fc.css.tascaeats.dto.UserRegistrationDTO;
import pt.ul.fc.css.tascaeats.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private static final String MSG_FIELD = "message";
    private static final String ERROR_FIELD = "error";

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegistrationDTO userRegistrationDTO) {
        try {
            User created = userService.createUser(userRegistrationDTO);
            Map<String, Object> response = new HashMap<>();
            response.put(MSG_FIELD, "User registered successfully");
            response.put("id", created.getId());
            // Escape user-provided fields before returning them in the response body.
            response.put("email", sanitizeForOutput(created.getEmail()));
            response.put("type", created.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(ERROR_FIELD, "Invalid registration data"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(ERROR_FIELD, "User already exists"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginDTO userLoginDTO) {
        boolean ok = userService.login(userLoginDTO);
        if (!ok) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(MSG_FIELD, "Invalid user"));
        }
        return ResponseEntity.ok(Map.of(MSG_FIELD, "Login successful (mock)"));
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable("id") Long id) {
        User user = userService.getUserById(id);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(ERROR_FIELD, "User not found"));
        }
        return ResponseEntity.ok(user);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable("id") Long id,
                                        @RequestBody UserRegistrationDTO userRegistrationDTO) {
        try {
            User updated = userService.updateUser(id, userRegistrationDTO);
            if (updated == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(ERROR_FIELD, "User not found"));
            }
            return ResponseEntity.ok(updated);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(ERROR_FIELD, "User update conflict"));
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable("id") Long id) {
        boolean deleted = userService.deleteUser(id);
        if (!deleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(ERROR_FIELD, "User not found"));
        }
        return ResponseEntity.noContent().build();
    }

    private String sanitizeForOutput(String value) {
        return value == null ? null : HtmlUtils.htmlEscape(value);
    }
}
