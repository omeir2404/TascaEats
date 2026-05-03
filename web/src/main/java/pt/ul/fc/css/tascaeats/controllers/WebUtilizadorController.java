// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################

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
            model.addAttribute("error", WebErrorMessages.backendFailure("Nao foi possivel carregar os utilizadores", e, backendUrl));
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
            model.addAttribute("error", "Login aceite pelo backend, mas nao encontrei os dados do utilizador para criar a sessao. Confirma se o utilizador existe na lista.");
            return "login";
        } catch (Exception e) {
            model.addAttribute("error", WebErrorMessages.backendFailure("Nao foi possivel iniciar sessao", e, backendUrl));
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
            @RequestParam(required = false) String rua,
            @RequestParam(required = false) Integer numero,
            @RequestParam(required = false) String complemento,
            @RequestParam(required = false) String codigoPostal,
            @RequestParam(required = false) String cidade,
            @RequestParam(required = false) String distrito,
            @RequestParam(required = false) String pais,
            Model model) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("name", nome);
            payload.put("email", email);
            payload.put("password", password);
            payload.put("type", mapPapelToBackendType(papel));

            Map<String, Object> address = buildOptionalAddressForClient(papel, rua, numero, complemento, codigoPostal, cidade, distrito, pais);
            if (address != null) {
                payload.put("address", address);
            }

            restTemplate.postForEntity(backendUrl + "/api/register", payload, Map.class);
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", WebErrorMessages.validationFailure("Nao foi possivel criar a conta", e));
            return "registo";
        } catch (Exception e) {
            model.addAttribute("error", WebErrorMessages.backendFailure("Nao foi possivel criar a conta", e, backendUrl));
            return "registo";
        }
    }

    // --- 3. PROFILE (VIEW & UPDATE) ---
    @GetMapping("/perfil/{id}")
    public String showProfile(@PathVariable Long id, Model model, HttpSession session) {
        if (!isAuthorizedUser(id, session)) {
            return "redirect:/login";
        }

        moveFlashAttribute(session, model, "profileSuccess");
        moveFlashAttribute(session, model, "profileError");

        try {
            Map<String, Object> user = restTemplate.getForObject(backendUrl + "/api/Utilizadors/" + id, Map.class);
            if (user != null) {
                model.addAttribute("user", normalizeUser(user));
                return "perfil";
            }
        } catch (Exception e) {
            model.addAttribute("error", WebErrorMessages.backendFailure("Nao foi possivel carregar o perfil", e, backendUrl));
        }
        return "redirect:/utilizadores";
    }

    @PostMapping("/perfil/{id}/atualizar")
    public String updateProfile(
            @PathVariable Long id,
            @RequestParam String nome, 
            @RequestParam String email,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String rua,
            @RequestParam(required = false) Integer numero,
            @RequestParam(required = false) String complemento,
            @RequestParam(required = false) String codigoPostal,
            @RequestParam(required = false) String cidade,
            @RequestParam(required = false) String distrito,
            @RequestParam(required = false) String pais,
            @RequestParam(required = false) Boolean available,
            HttpSession session) {
        if (!isAuthorizedUser(id, session)) {
            return "redirect:/login";
        }

        try {
            Map<String, Object> currentUser = restTemplate.getForObject(backendUrl + "/api/Utilizadors/" + id, Map.class);
            Map<String, Object> normalizedUser = currentUser == null ? Map.of() : normalizeUser(currentUser);
            String papel = String.valueOf(normalizedUser.getOrDefault("papel", "CLIENT"));

            Map<String, Object> payload = new HashMap<>();
            payload.put("name", nome);
            payload.put("email", email);
            payload.put("type", mapPapelToBackendType(papel));
            if (hasText(password)) {
                payload.put("password", password.trim());
            }
            if ("COURIER".equalsIgnoreCase(papel)) {
                payload.put("available", Boolean.TRUE.equals(available));
            }

            Map<String, Object> address = buildOptionalAddressForClient(papel, rua, numero, complemento, codigoPostal, cidade, distrito, pais);
            if (address != null) {
                payload.put("address", address);
            }

            restTemplate.exchange(
                    backendUrl + "/api/Utilizadors/" + id,
                    HttpMethod.PUT,
                    new HttpEntity<>(payload),
                    Map.class
            );

            Optional<Map<String, Object>> updatedUser = findUserByEmail(email);
            updatedUser.ifPresent(user -> session.setAttribute("loggedUser", user));
            session.setAttribute("profileSuccess", "Perfil atualizado com sucesso.");
        } catch (IllegalArgumentException e) {
            session.setAttribute("profileError", WebErrorMessages.validationFailure("Nao foi possivel atualizar o perfil", e));
            return "redirect:/perfil/" + id;
        } catch (Exception e) {
            session.setAttribute("profileError", WebErrorMessages.backendFailure("Nao foi possivel atualizar o perfil", e, backendUrl));
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
            session.setAttribute("profileError", WebErrorMessages.backendFailure("Nao foi possivel eliminar a conta", e, backendUrl));
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
        String papel = mapBackendTypeToPapel(type);
        boolean ativo = true;
        if ("COURIER".equalsIgnoreCase(papel)) {
            ativo = Boolean.TRUE.equals(user.get("available"));
        }

        Map<String, Object> normalized = new HashMap<>();
        normalized.put("id", user.getOrDefault("id", 0));
        normalized.put("nome", user.getOrDefault("name", ""));
        normalized.put("email", user.getOrDefault("email", ""));
        normalized.put("inicial", buildInitial(String.valueOf(user.getOrDefault("name", ""))));
        normalized.put("tipoOriginal", type);
        normalized.put("papel", papel);
        normalized.put("ativo", ativo);
        normalized.put("cliente", "CLIENT".equalsIgnoreCase(papel));
        normalized.put("entregador", "COURIER".equalsIgnoreCase(papel));
        normalized.put("morada", normalizeAddress(user.get("address")));
        normalized.put("moradaResumo", summarizeAddress((Map<String, Object>) normalized.get("morada")));
        return normalized;
    }

    private Map<String, Object> normalizeAddress(Object addressObj) {
        Map<String, Object> address = new HashMap<>();
        address.put("rua", "");
        address.put("numero", "");
        address.put("complemento", "");
        address.put("codigoPostal", "");
        address.put("cidade", "");
        address.put("distrito", "");
        address.put("pais", "");

        if (addressObj instanceof Map<?, ?> rawAddress) {
            address.put("rua", valueOrBlank(rawAddress.get("rua")));
            address.put("numero", valueOrBlank(rawAddress.get("numero")));
            address.put("complemento", valueOrBlank(rawAddress.get("complemento")));
            address.put("codigoPostal", valueOrBlank(rawAddress.get("codigoPostal")));
            address.put("cidade", valueOrBlank(rawAddress.get("cidade")));
            address.put("distrito", valueOrBlank(rawAddress.get("distrito")));
            address.put("pais", valueOrBlank(rawAddress.get("pais")));
        }

        return address;
    }

    private String valueOrBlank(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String buildInitial(String name) {
        if (!hasText(name)) {
            return "T";
        }
        return name.trim().substring(0, 1);
    }

    private String summarizeAddress(Map<String, Object> address) {
        if (address == null || !hasText(String.valueOf(address.get("rua")))) {
            return "Sem morada definida";
        }

        StringBuilder summary = new StringBuilder();
        summary.append(address.get("rua"));
        if (hasText(String.valueOf(address.get("numero")))) {
            summary.append(", ").append(address.get("numero"));
        }
        if (hasText(String.valueOf(address.get("cidade")))) {
            summary.append(" - ").append(address.get("cidade"));
        }
        return summary.toString();
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

    private Map<String, Object> buildOptionalAddressForClient(
            String papel,
            String rua,
            Integer numero,
            String complemento,
            String codigoPostal,
            String cidade,
            String distrito,
            String pais) {
        if (!"CLIENT".equalsIgnoreCase(papel)) {
            return null;
        }

        boolean hasAnyField = hasText(rua)
                || numero != null
                || hasText(complemento)
                || hasText(codigoPostal)
                || hasText(cidade)
                || hasText(distrito)
                || hasText(pais);

        if (!hasAnyField) {
            return null;
        }

        if (!hasText(rua)
                || numero == null
                || numero <= 0
                || !hasText(codigoPostal)
                || !hasText(cidade)
                || !hasText(distrito)
                || !hasText(pais)) {
            throw new IllegalArgumentException("If you provide an address, all mandatory address fields must be filled.");
        }

        Map<String, Object> address = new HashMap<>();
        address.put("rua", rua.trim());
        address.put("numero", numero);
        address.put("complemento", hasText(complemento) ? complemento.trim() : "");
        address.put("codigoPostal", codigoPostal.trim());
        address.put("cidade", cidade.trim());
        address.put("distrito", distrito.trim());
        address.put("pais", pais.trim());
        return address;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private void moveFlashAttribute(HttpSession session, Model model, String attributeName) {
        Object value = session.getAttribute(attributeName);
        if (value != null) {
            model.addAttribute(attributeName, value);
            session.removeAttribute(attributeName);
        }
    }
}
