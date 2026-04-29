package pt.ul.fc.css.tascaeats.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

@Controller
public class WebUtilizadorController {

    // --- MUTABLE MOCK DATABASE ---
    // Using HashMap so we can edit and delete users while the app is running
    private List<Map<String, Object>> mockUsers = new ArrayList<>(Arrays.asList(
        new HashMap<>(Map.of("id", 1, "nome", "João Silva", "email", "joao@email.com", "papel", "CLIENT", "ativo", true)),
        new HashMap<>(Map.of("id", 2, "nome", "Maria Santos", "email", "maria@email.com", "papel", "ADMIN", "ativo", true)),
        new HashMap<>(Map.of("id", 3, "nome", "Carlos Ferreira", "email", "carlos@email.com", "papel", "COURIER", "ativo", false)),
        new HashMap<>(Map.of("id", 4, "nome", "Ana Costa", "email", "ana@email.com", "papel", "CLIENT", "ativo", false))
    ));
    private int nextId = 5;

    // --- USERS LIST ---
    @GetMapping("/utilizadores")
    public String listarUtilizadores(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String papel,
            @RequestParam(required = false, defaultValue = "false") Boolean apenasAtivos,
            Model model) {

        List<Map<String, Object>> filtrados = mockUsers.stream()
                .filter(u -> nome == null || nome.isBlank() || u.get("nome").toString().toLowerCase().contains(nome.toLowerCase()))
                .filter(u -> papel == null || papel.isBlank() || u.get("papel").toString().equalsIgnoreCase(papel))
                .filter(u -> !apenasAtivos || (Boolean) u.get("ativo") == true)
                .toList();

        model.addAttribute("utilizadores", filtrados);
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
        
        // Find the user by email
        Optional<Map<String, Object>> loggedInUser = mockUsers.stream()
            .filter(u -> u.get("email").equals(email))
            .findFirst();
        
        if (loggedInUser.isPresent()) {
            // SUCCESS! Save the user's data into the global session
            session.setAttribute("loggedUser", loggedInUser.get());
            return "redirect:/restaurantes"; 
        }
        
        model.addAttribute("error", "Invalid credentials. User not found.");
        return "login"; 
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
            @RequestParam String papel) {
        
        Map<String, Object> newUser = new HashMap<>();
        newUser.put("id", nextId++);
        newUser.put("nome", nome);
        newUser.put("email", email);
        newUser.put("papel", papel);
        newUser.put("ativo", true);
        
        mockUsers.add(newUser);
        return "redirect:/login"; // Redirect to login after successful registration
    }

    // --- 3. PROFILE (VIEW & UPDATE) ---
    @GetMapping("/perfil/{id}")
    public String showProfile(@PathVariable Integer id, Model model) {
        Optional<Map<String, Object>> user = mockUsers.stream().filter(u -> u.get("id").equals(id)).findFirst();
        
        if (user.isPresent()) {
            model.addAttribute("user", user.get());
            return "perfil";
        }
        return "redirect:/utilizadores"; // If user doesn't exist, go back to list
    }

    @PostMapping("/perfil/{id}/atualizar")
    public String updateProfile(
            @PathVariable Integer id, 
            @RequestParam String nome, 
            @RequestParam String email) {
        
        mockUsers.stream().filter(u -> u.get("id").equals(id)).findFirst().ifPresent(u -> {
            u.put("nome", nome);
            u.put("email", email);
        });
        
        return "redirect:/perfil/" + id; // Reload profile to see changes
    }

    // --- 4. DELETE ACCOUNT ---
    @PostMapping("/perfil/{id}/apagar")
    public String deleteAccount(@PathVariable Integer id) {
        mockUsers.removeIf(u -> u.get("id").equals(id));
        return "redirect:/utilizadores"; // Go back to the users list after deletion
    }
}