// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################


package pt.ul.fc.css.tascaeats.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import static pt.ul.fc.css.tascaeats.ui.UiUtils.*;

public class RegisterPane {

    private final AppState state;
    private final Stage owner;
    private final Runnable onBackToLogin;

    public RegisterPane(AppState state, Stage owner, Runnable onBackToLogin) {
        this.state = state;
        this.owner = owner;
        this.onBackToLogin = onBackToLogin;
    }

    public VBox build() {
        Label title = sectionTitle("Registar Utilizador");

        TextField nomeF = new TextField();
        nomeF.setPromptText("Nome");
        TextField emailF = new TextField();
        emailF.setPromptText("email@exemplo.com");
        PasswordField passF = new PasswordField();
        passF.setPromptText("password");
        ComboBox<String> papelBox = combo("ADMIN", "COURIER");

        GridPane form = formGrid();
        form.add(lbl("Nome:"), 0, 0); form.add(nomeF, 1, 0);
        form.add(lbl("Email:"), 0, 1); form.add(emailF, 1, 1);
        form.add(lbl("Password:"), 0, 2); form.add(passF, 1, 2);
        form.add(lbl("Papel:"), 0, 3); form.add(papelBox, 1, 3);

        Label feedback = new Label();
        Button registerBtn = primaryButton("Registar");
        registerBtn.setDefaultButton(true);
        registerBtn.setOnAction(e -> handleRegister(
            nomeF.getText().trim(),
            emailF.getText().trim(),
            passF.getText(),
            papelBox.getValue(),
            feedback
        ));

        Button backBtn = new Button("Voltar ao login");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #1565c0; -fx-border-color: transparent;");
        backBtn.setOnAction(e -> onBackToLogin.run());

        VBox box = centeredCard(title, form, registerBtn, feedback, backBtn);
        box.setMaxWidth(420);
        VBox wrapper = new VBox(box);
        wrapper.setAlignment(Pos.TOP_CENTER);
        wrapper.setPadding(new Insets(40));
        wrapper.setStyle("-fx-background-color: #f8f9fa;");
        VBox.setVgrow(wrapper, Priority.ALWAYS);
        return wrapper;
    }

    private void handleRegister(String nome, String email, String pass, String papel, Label feedback) {
        if (nome.isEmpty()) { err(feedback, "✗ Digite o nome."); return; }
        if (email.isEmpty()) { err(feedback, "✗ Digite o email."); return; }
        if (pass == null || pass.isBlank()) { err(feedback, "✗ Digite a password."); return; }

        String tipo = mapPapelToTipo(papel);
        UtilizadorApiClient.RegistoResult res = state.utilizadorClient.registarUtilizador(nome, email, pass, tipo);
        if (res.fallbackUsado()) {
            err(feedback, "⚠ " + res.mensagem());
        } else if (res.id() != null) {
            ok(feedback, "✓ Utilizador registado. Já pode iniciar sessão.");
        } else {
            err(feedback, "✗ " + res.mensagem());
        }
    }

    private String mapPapelToTipo(String papel) {
        return switch (papel) {
            case "ADMIN" -> "restaurant owner";
            case "COURIER" -> "driver";
            default -> "client";
        };
    }
}
