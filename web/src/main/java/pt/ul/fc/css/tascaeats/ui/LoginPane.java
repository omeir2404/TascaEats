package pt.ul.fc.css.tascaeats.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.Map;
import java.util.Optional;

import static pt.ul.fc.css.tascaeats.ui.UiUtils.*;

/**
 * Use Case A – Login.
 * Mock login: any password accepted as long as the user exists in the backend.
 */
public class LoginPane {

    private final AppState state;
    private final Stage    owner;
    private final Runnable onLoginSuccess;
    private final Runnable onRegisterRequested;

    public LoginPane(AppState state, Stage owner, Runnable onLoginSuccess, Runnable onRegisterRequested) {
        this.state               = state;
        this.owner               = owner;
        this.onLoginSuccess      = onLoginSuccess;
        this.onRegisterRequested = onRegisterRequested;
    }

    public VBox build() {
        Label title = sectionTitle("A – Login");

        TextField     emailF = new TextField();  emailF.setPromptText("email@exemplo.com");
        PasswordField passF  = new PasswordField(); passF.setPromptText("password");

        GridPane form = formGrid();
        form.add(lbl("Email:"),    0, 0); form.add(emailF, 1, 0);
        form.add(lbl("Password:"), 0, 1); form.add(passF,  1, 1);

        Label feedback = new Label();
        Button loginBtn = primaryButton("Entrar");
        loginBtn.setDefaultButton(true);
        loginBtn.setOnAction(e -> handleLogin(emailF.getText().trim(), passF.getText(), feedback));

        Button regBtn = new Button("Ainda não tens conta? Registar");
        regBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #1565c0; -fx-border-color: transparent;");
        regBtn.setOnAction(e -> onRegisterRequested.run());

        VBox box = centeredCard(title, form, loginBtn, feedback, regBtn);
        box.setMaxWidth(420);
        VBox wrapper = new VBox(box);
        wrapper.setAlignment(Pos.TOP_CENTER);
        wrapper.setPadding(new Insets(40));
        wrapper.setStyle("-fx-background-color: #f8f9fa;");
        VBox.setVgrow(wrapper, Priority.ALWAYS);
        return wrapper;
    }

    private void handleLogin(String email, String pass, Label feedback) {
        if (email.isEmpty()) { err(feedback, "✗ Digite o email."); return; }

        UtilizadorApiClient.LoginResult result = state.utilizadorClient.login(email, pass);

        if (result.sucesso()) {
            // Fetch real user from backend to get id/role
            state.bg(() -> state.utilizadorClient.listarUtilizadores(), allUsers -> {
                Optional<Map<String, Object>> found = allUsers.stream()
                    .filter(u -> email.equalsIgnoreCase(s(u, "email")))
                    .findFirst();
                if (found.isPresent()) {
                    String papel = s(found.get(), "papel");
                    if ("CLIENT".equalsIgnoreCase(papel) || "CLIENTE".equalsIgnoreCase(papel)) {
                        err(feedback, "✗ Acesso negado. A aplicação nativa é exclusiva para Staff.");
                        return;
                    }
                    state.currentUser = found.get();
                } else {
                    // Authenticated but not in list — minimal fallback entry
                    state.currentUser = row("id", "?", "nome", email.split("@")[0],
                        "email", email, "papel", "ADMIN", "ativo", "Sim", "password", "");
                }
                ok(feedback, "✓ Login bem-sucedido.");
                onLoginSuccess.run();
            });
        } else if (result.fallbackUsado()) {
            err(feedback, "⚠ Backend indisponível.");
        } else {
            err(feedback, "✗ " + result.mensagem());
        }
    }
}
