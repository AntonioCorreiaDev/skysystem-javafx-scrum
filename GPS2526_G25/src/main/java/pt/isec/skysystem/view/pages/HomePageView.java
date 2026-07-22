package pt.isec.skysystem.view.pages;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.image.Image;
import javafx.util.Duration;
import pt.isec.skysystem.SkySystemApp;
import pt.isec.skysystem.model.DataFacade;
import javafx.scene.image.ImageView;
import pt.isec.skysystem.viewmodel.HomePageViewModel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the Home Page
 */
public class HomePageView {
    private static final Logger logger = LoggerFactory.getLogger(HomePageView.class);

    @FXML
    StackPane heroContainer;
    @FXML
    private Button quizButton;
    @FXML
    private Button askAiButton;
    @FXML
    private Button searchButton;
    @FXML
    private Button preferencesButton;
    @FXML
    private Button dashboardButton;
    @FXML
    private Button logoutButton;
    @FXML
    private ImageView heroBackground;
    @FXML
    private Label heroMainText;

    private HomePageViewModel viewModel;

    private final List<Image> backgroundImages = new ArrayList<>();
    private int currentImageIndex = 0;
    private Timeline slideshowTimeline;

    @FXML
    public void initialize() {
        this.viewModel = new HomePageViewModel();
        setupBindings();

        setupSlideshow();

        logger.info("HomePageView initialized");
    }

    private void setupBindings() {
        quizButton.textProperty().bind(viewModel.quizButtonTextProperty());
        askAiButton.disableProperty().bind(viewModel.aiAssistantEnabledProperty().not());
        heroMainText.textProperty().bind(viewModel.heroGreetingProperty());

        viewModel.aiAssistantEnabledProperty().addListener((obs, oldVal, newVal) -> {
            askAiButton.setOpacity(newVal ? 1.0 : 0.5);
        });

        askAiButton.setOpacity(viewModel.isAiAssistantEnabled() ? 1.0 : 0.5);
    }

    /**
     * Configura o slideshow com imagens da WEB
     */
    private void setupSlideshow() {
        List<String> urlsToLoad = viewModel.getSlideshowImageUrls();

        for (String path : urlsToLoad) {
            loadImageSafe(path);
        }

        // iniciar a animação se houver imagens e o componente visual existir
        if (!backgroundImages.isEmpty() && heroBackground != null) {
            heroBackground.setImage(backgroundImages.get(0));

            if (backgroundImages.size() > 1) {
                slideshowTimeline = new Timeline(new KeyFrame(Duration.seconds(10), event -> {
                    transitionToNextImage();
                }));
                slideshowTimeline.setCycleCount(Timeline.INDEFINITE);
                slideshowTimeline.play();
            }
        } else {
            logger.warn("Slideshow not started: No images loaded or heroBackground is null.");
        }
    }

    /**
     * Carrega imagem da Web
     */
    private void loadImageSafe(String path) {
        try {
            Image img = new Image(path, true);
            backgroundImages.add(img);
        } catch (Exception e) {
            logger.error("Failed to load image: " + path);
        }
    }

    private void transitionToNextImage() {
        if (heroBackground == null)
            return;

        FadeTransition fadeOut = new FadeTransition(Duration.millis(1000), heroBackground);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        fadeOut.setOnFinished(e -> {
            currentImageIndex = (currentImageIndex + 1) % backgroundImages.size();

            Image nextImg = backgroundImages.get(currentImageIndex);

            heroBackground.setImage(nextImg);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(700), heroBackground);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });

        fadeOut.play();
    }

    /**
     * Refresh ViewModel state
     */
    public void refresh() {
        viewModel.updateUserStatus();
    }

    /**
     * Handle Quiz button click
     * Delegates to navigation only
     */
    @FXML
    private void handleQuizButton() {
        try {
            logger.info("Navigating to Quiz");
            DataFacade.getInstance().setLastPage("HomePage");
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/OnboardingQuiz.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) quizButton.getScene().getWindow();
            Scene scene = new Scene(root, SkySystemApp.WINDOW_WIDTH, SkySystemApp.WINDOW_HEIGHT);
            stage.setScene(scene);
            stage.setTitle("Sky System - Onboarding Quiz");

        } catch (IOException e) {
            logger.error("Failed to load quiz page", e);
            showError("Failed to load quiz page: " + e.getMessage());
        }
    }

    /**
     * Handle Ask AI button click
     */
    @FXML
    private void handleAskAiButton() {
        if (!viewModel.hasCompletedOnboarding()) {
            showInfo("Please complete the quiz first!",
                    "You need to complete the onboarding quiz before using the AI Assistant.");
            return;
        }

        try {
            logger.info("Navigating to Inspire Me");
            DataFacade.getInstance().setLastPage("HomePage");
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/ai_suggestions.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) quizButton.getScene().getWindow();
            Scene scene = new Scene(root, SkySystemApp.WINDOW_WIDTH, SkySystemApp.WINDOW_HEIGHT);
            stage.setScene(scene);
            stage.setTitle("Sky System - Inspire Me");
        } catch (IOException e) {
            logger.error("Failed to load Inspire Me page", e);
            showError("Failed to load Inspire Me page: " + e.getMessage());
        }
    }

    /**
     * Handle Search button
     */
    @FXML
    private void handleSearchButton() {
        try {
            logger.info("Navigating to Search");
            DataFacade.getInstance().setLastPage("HomePage");
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/BasicFlightSearch.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) searchButton.getScene().getWindow();
            Scene scene = new Scene(root, SkySystemApp.WINDOW_WIDTH, SkySystemApp.WINDOW_HEIGHT);
            stage.setScene(scene);
            stage.setTitle("Sky System - Search");

        } catch (IOException e) {
            logger.error("Failed to load Search page", e);
            showError("Failed to load Search page: " + e.getMessage());
        }
    }

    /**
     * Handle Preferences button
     */
    @FXML
    private void handlePreferencesButton() {
        try {
            logger.info("Navigating to Preferences");
            DataFacade.getInstance().setLastPage("HomePage");
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/Preferences.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) preferencesButton.getScene().getWindow();
            Scene scene = new Scene(root, SkySystemApp.WINDOW_WIDTH, SkySystemApp.WINDOW_HEIGHT);
            stage.setScene(scene);
            stage.setTitle("Sky System - Preferences");

        } catch (IOException e) {
            logger.error("Failed to load Preferences page", e);
            showError("Failed to load Preferences page: " + e.getMessage());
        }
    }

    /**
     * Handle Dashboard button - NEW!
     */
    @FXML
    private void handleDashboardButton() {
        try {
            logger.info("Navigating to Dashboard");
            DataFacade.getInstance().setLastPage("HomePage");
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/DashBoard.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) dashboardButton.getScene().getWindow();
            Scene scene = new Scene(root, SkySystemApp.WINDOW_WIDTH, SkySystemApp.WINDOW_HEIGHT);
            stage.setScene(scene);
            stage.setTitle("Sky System - Dashboard");

        } catch (IOException e) {
            logger.error("Failed to load Dashboard page", e);
            showError("Failed to load Dashboard page: " + e.getMessage());
        }
    }

    /**
     * Handle Exit button
     */
    @FXML
    private void handleLogoutButton() {
        logger.info("Logging out...");
        viewModel.logout();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) logoutButton.getScene().getWindow();
            Scene scene = new Scene(root, SkySystemApp.WINDOW_WIDTH, SkySystemApp.WINDOW_HEIGHT);
            stage.setScene(scene);
            stage.setTitle("Sky System - Login");

        } catch (IOException e) {
            logger.error("Failed to load Login page after logout", e);
            showError("Failed to load Login page: " + e.getMessage());
        }
    }

    // ===== HELPER METHODS =====

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}