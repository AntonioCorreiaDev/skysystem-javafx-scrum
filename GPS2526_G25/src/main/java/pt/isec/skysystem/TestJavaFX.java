package pt.isec.skysystem;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import pt.isec.skysystem.model.DataFacade;

import java.io.IOException;

public class TestJavaFX extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        DataFacade facade = DataFacade.getInstance();
        facade.setCurrentUser("visualTestUser", "email");

        if (!facade.currentUserNeedsOnboarding()) {
            facade.resetCurrentUserPreferences();
        }

        System.out.println("A carregar quiz para o utilizador: "
                + facade.getCurrentUser().getUsername());

        // Carregar FXML
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/Preferences.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, SkySystemApp.WINDOW_WIDTH, SkySystemApp.WINDOW_HEIGHT);
        stage.setTitle(SkySystemApp.APP_TITLE + " - Teste do Quiz Onboarding");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}