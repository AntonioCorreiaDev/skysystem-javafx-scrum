package pt.isec.skysystem.view.pages;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import pt.isec.skysystem.model.DataFacade;
import pt.isec.skysystem.SkySystemApp;
import java.io.IOException;

public class RegistrationController {

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;

    private final DataFacade dataFacade = DataFacade.getInstance();

    @FXML
    private void handleRegister() {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            errorLabel.setText("All fields are required.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            errorLabel.setText("Passwords do not match.");
            return;
        }

        if (dataFacade.register(username, password, email)) {
            // Auto-login after successful registration
            if (dataFacade.login(email, password)) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/HomePage.fxml"));
                    Parent root = loader.load();
                    Stage stage = SkySystemApp.getPrimaryStage();
                    stage.getScene().setRoot(root);
                } catch (IOException e) {
                    errorLabel.setText("Registration successful, but failed to load Home Page.");
                    e.printStackTrace();
                }
            } else {
                errorLabel.setText("Registration successful, but auto-login failed.");
            }
        } else {
            errorLabel.setText("Registration failed. Username or Email might already exist.");
        }
    }

    @FXML
    private void handleBackToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();
            Stage stage = SkySystemApp.getPrimaryStage();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            errorLabel.setText("Error loading Login Page.");
            e.printStackTrace();
        }
    }
}
