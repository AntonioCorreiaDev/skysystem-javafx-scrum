package pt.isec.skysystem.view.pages;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import pt.isec.skysystem.model.DataFacade;
import pt.isec.skysystem.SkySystemApp;
import java.io.IOException;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final DataFacade dataFacade = DataFacade.getInstance();

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter both email and password.");
            return;
        }

        if (dataFacade.login(email, password)) {
            try {
                // Navigate to Home Page
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/HomePage.fxml"));
                Parent root = loader.load();
                Stage stage = SkySystemApp.getPrimaryStage();
                stage.getScene().setRoot(root);
            } catch (IOException e) {
                errorLabel.setText("Error loading Home Page.");
                e.printStackTrace();
            }
        } else {
            errorLabel.setText("Invalid email or password.");
        }
    }

    @FXML
    private void handleRegisterLink() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Registration.fxml"));
            Parent root = loader.load();
            Stage stage = SkySystemApp.getPrimaryStage();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            errorLabel.setText("Error loading Registration Page.");
            e.printStackTrace();
        }
    }
}
