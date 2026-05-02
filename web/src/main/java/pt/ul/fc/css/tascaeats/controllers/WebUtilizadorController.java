package pt.ul.fc.css.tascaeats.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpSession;

@Controller
public class WebUtilizadorController {

    private final RestTemplate restTemplate;

    @Value("${BACKEND_API_URL:http://localhost:8080}")
    private String backendUrl;

    public WebUtilizadorController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // --- USERS LIST ---
    @GetMapping("/utilizadores")
    public String listarUtilizadores(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String papel,
            @RequestParam(required = false, defaultValue = "false") Boolean apenasAtivos,
            Model model) {

        try {
            List<Map<String, Object>> utilizadores = restTemplate.getForObject(backendUrl + "/api/Utilizadors", List.class);
            List<Map<String, Object>> filtrados = normalizeUsers(utilizadores).stream()
                    .filter(u -> nome == null || nome.isBlank() || u.get("nome").toString().toLowerCase().contains(nome.toLowerCase()))
                    .filter(u -> papel == null || papel.isBlank() || u.get("papel").toString().equalsIgnoreCase(papel))
                    .filter(u -> !Boolean.TRUE.equals(apenasAtivos) || Boolean.TRUE.equals(u.get("ativo")))
                    .toList();
            model.addAttribute("utilizadores", filtrados);
        } catch (Exception e) {
            model.addAttribute("utilizadores", List.of());
            model.addAttribute("error", "Could not load users from backend.");
        }

        model.addAttribute("nome", nome);
        model.addAttribute("papel", papel);
        model.addAttribute("apenasAtivos", apenasAtivos);

        return "utilizadores";
    }

    // --- 1. LOGIN ---
    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String email, @RequestParam String password, Model model, HttpSession session) {
        try {
            Map<String, Object> loginPayload = Map.of(
                    "email", email,
                    "password", password
            );

            ResponseEntity<Map> loginResponse = restTemplate.postForEntity(backendUrl + "/api/login", loginPayload, Map.class);
            if (!loginResponse.getStatusCode().is2xxSuccessful()) {
                model.addAttribute("error", "Invalid credentials.");
                return "login";
            }

            Optional<Map<String, Object>> loggedInUser = findUserByEmail(email);
            if (loggedInUser.isPresent()) {
                session.setAttribute("loggedUser", loggedInUser.get());
                return "redirect:/restaurantes";
            }
            model.addAttribute("error", "User authenticated but not found in user list.");
            return "login";
        } catch (Exception e) {
            model.addAttribute("error", "Login failed: could not connect to backend.");
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Destroys the session, making the server forget the user
        return "redirect:/"; // Go back to the homepage
    }

    // --- 2. REGISTRATION ---
    @GetMapping("/registo")
    public String showRegistrationForm() {
        return "registo";
    }

    @PostMapping("/registo")
    public String processRegistration(
            @RequestParam String nome, 
            @RequestParam String email, 
            @RequestParam String password, 
            @RequestParam String papel,
            Model model) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("name", nome);
            payload.put("email", email);
            payload.put("password", password);
            payload.put("type", mapPapelToBackendType(papel));

            Map<String, Object> address = buildDefaultAddressIfClient(papel);
            if (address != null) {
                payload.put("address", address);
            }

            restTemplate.postForEntity(backendUrl + "/api/register", payload, Map.class);
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed. Check the data and try again.");
            return "registo";
        }
    }

    // --- 3. PROFILE (VIEW & UPDATE) ---
    @GetMapping("/perfil/{id}")
    public String showProfile(@PathVariable Long id, Model model, HttpSession session) {
        if (!isAuthorizedUser(id, session)) {
            return "redirect:/login";
        }

        try {
            Map<String, Object> user = restTemplate.getForObject(backendUrl + "/api/Utilizadors/" + id, Map.class);
            if (user != null) {
                model.addAttribute("user", normalizeUser(user));
                return "perfil";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Could not load user profile.");
        }
        return "redirect:/utilizadores";
    }

    @PostMapping("/perfil/{id}/atualizar")
    public String updateProfile(
            @PathVariable Long id,
            @RequestParam String nome, 
            @RequestParam String email,
            HttpSession session) {
        if (!isAuthorizedUser(id, session)) {
            return "redirect:/login";
        }

        try {
            Map<String, Object> payload = Map.of(
                    "name", nome,
                    "email", email
            );

            restTemplate.exchange(
                    backendUrl + "/api/Utilizadors/" + id,
                    HttpMethod.PUT,
                    new HttpEntity<>(payload),
                    Map.class
            );

            Optional<Map<String, Object>> updatedUser = findUserByEmail(email);
            updatedUser.ifPresent(user -> session.setAttribute("loggedUser", user));
        } catch (Exception e) {
            return "redirect:/perfil/" + id;
        }

        return "redirect:/perfil/" + id;
    }

    // --- 4. DELETE ACCOUNT ---
    @PostMapping("/perfil/{id}/apagar")
    public String deleteAccount(@PathVariable Long id, HttpSession session) {
        if (!isAuthorizedUser(id, session)) {
            return "redirect:/login";
        }

        try {
            restTemplate.delete(backendUrl + "/api/Utilizadors/" + id);
            session.invalidate();
        } catch (Exception e) {
            return "redirect:/perfil/" + id;
        }
        return "redirect:/utilizadores";
    }

    private List<Map<String, Object>> normalizeUsers(List<Map<String, Object>> backendUsers) {
        if (backendUsers == null) {
            return List.of();
        }
        return backendUsers.stream().map(this::normalizeUser).toList();
    }

    private Map<String, Object> normalizeUser(Map<String, Object> user) {
        String type = String.valueOf(user.getOrDefault("type", "client"));
        boolean ativo = true;
        if ("entregador".equalsIgnoreCase(type) || "driver".equalsIgnoreCase(type)) {
            ativo = Boolean.TRUE.equals(user.get("available"));
        }

        Map<String, Object> normalized = new HashMap<>();
        normalized.put("id", user.getOrDefault("id", 0));
        normalized.put("nome", user.getOrDefault("name", ""));
        normalized.put("email", user.getOrDefault("email", ""));
        normalized.put("papel", mapBackendTypeToPapel(type));
        normalized.put("ativo", ativo);
        return normalized;
    }

    private Optional<Map<String, Object>> findUserByEmail(String email) {
        String url = UriComponentsBuilder.fromHttpUrl(backendUrl + "/api/Utilizadors").toUriString();
        List<Map<String, Object>> users = restTemplate.getForObject(url, List.class);
        return normalizeUsers(users).stream()
                .filter(u -> email.equalsIgnoreCase(String.valueOf(u.get("email"))))
                .findFirst();
    }

    private boolean isAuthorizedUser(Long id, HttpSession session) {
        Object loggedUser = session.getAttribute("loggedUser");
        if (!(loggedUser instanceof Map<?, ?> userMap)) {
            return false;
        }

        Object sessionId = userMap.get("id");
        if (sessionId == null) {
            return false;
        }

        return Long.valueOf(String.valueOf(sessionId)).equals(id);
    }

    private String mapBackendTypeToPapel(String type) {
        String normalized = type.trim().toLowerCase();
        if (normalized.contains("entregador") || normalized.contains("driver")) {
            return "COURIER";
        }
        if (normalized.contains("admin") || normalized.contains("restaurante")) {
            return "ADMIN";
        }
        return "CLIENT";
    }

    private String mapPapelToBackendType(String papel) {
        return switch (papel.toUpperCase()) {
            case "COURIER" -> "driver";
            case "ADMIN" -> "admin";
            default -> "client";
        };
    }

    private Map<String, Object> buildDefaultAddressIfClient(String papel) {
        if (!"CLIENT".equalsIgnoreCase(papel)) {
            return null;
        }

        return Map.of(
                "rua", "Rua Principal",
                "numero", 1,
                "complemento", "",
                "codigoPostal", "1000-001",
                "cidade", "Lisboa",
                "distrito", "Lisboa",
                "pais", "Portugal"
        );
    }
}