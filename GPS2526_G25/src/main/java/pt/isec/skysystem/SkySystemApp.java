package pt.isec.skysystem;

import com.amadeus.exceptions.ResponseException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;

/**
 * Main Application class for Sky System Flight Reservation
 * Initializes the JavaFX application and sets up the initial view
 */
public class SkySystemApp extends Application {

        private static final Logger logger = LoggerFactory.getLogger(SkySystemApp.class);
        public static final String APP_TITLE = "Sky System - Flight Reservation";
        public static final int WINDOW_WIDTH = 1000;
        public static final int WINDOW_HEIGHT = 600;

        private static Stage primaryStage;

        @Override
        public void start(Stage stage) throws ResponseException, IOException {
                primaryStage = stage;

                FXMLLoader loader = new FXMLLoader(
                                getClass().getResource("/fxml/Login.fxml"));
                Parent root = loader.load();

                Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

                stage.setTitle(APP_TITLE);
                stage.setScene(scene);

                stage.setMinWidth(WINDOW_WIDTH);
                stage.setMinHeight(WINDOW_HEIGHT);
                stage.setResizable(true);

                stage.show();
        }

        @Override
        public void stop() {
                logger.info("Sky System application shutting down");
                // Cleanup resources here (close database connections, etc.)
        }

        /**
         * Returns the primary stage of the application
         */
        public static Stage getPrimaryStage() {
                return primaryStage;
        }

        /**
         * Main entry point for the application
         */
        public static void main(String[] args) {
                logger.info("Launching Sky System application...");
                launch(args);
        }
}