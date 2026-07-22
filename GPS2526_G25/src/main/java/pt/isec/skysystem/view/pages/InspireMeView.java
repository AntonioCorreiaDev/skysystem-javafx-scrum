package pt.isec.skysystem.view.pages;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import pt.isec.skysystem.SkySystemApp;
import pt.isec.skysystem.model.data.Controllers.SuggestionController;
import pt.isec.skysystem.model.data.Suggestion;
import pt.isec.skysystem.viewmodel.InspireMeViewModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * Esta é a classe View/Controller para o ai_suggestions.fxml.
 */
public class InspireMeView implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(InspireMeView.class);
    private InspireMeViewModel viewModel;

    @FXML
    private VBox sugestoesContainerVBox;
    @FXML
    private Button backButton;
    @FXML
    private Button preferencesButton;
    @FXML
    private Button refreshAllButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.viewModel = new InspireMeViewModel();
        refreshAllButton.disableProperty().bind(viewModel.isLoadingProperty());
        sugestoesContainerVBox.disableProperty().bind(viewModel.isLoadingProperty());
        viewModel.isLoadingProperty().addListener((obs, wasLoading, isNowLoading) -> {
            if (isNowLoading) {
                refreshAllButton.setText("Refreshing suggestions...");
                sugestoesContainerVBox.getScene().setCursor(javafx.scene.Cursor.WAIT);
            } else {
                refreshAllButton.setText("Refresh suggestions");
                sugestoesContainerVBox.getScene().setCursor(javafx.scene.Cursor.DEFAULT);
            }
        });
        // listener to suggestions list
        viewModel.getSuggestionsList().addListener((javafx.collections.ListChangeListener<Suggestion>) c -> {
            populateSuggestionCards();
        });
        // check pre-loading
        if (!viewModel.getSuggestionsList().isEmpty()) {
            populateSuggestionCards();
        } else {
            handleRefreshAllButton();
        }
    }

    /**
     * Percorre a lista de sugestões no ViewModel e cria um card FXML
     * para cada uma, adicionando-o ao VBox.
     */
    private void populateSuggestionCards() {
        if (sugestoesContainerVBox == null) {
            logger.error("CRITICAL ERROR: sugestoesContainerVBox is NULL!");
            return;
        }

        sugestoesContainerVBox.getChildren().clear();

        // Itera sobre a lista de SUGESTÕES
        for (Suggestion suggestion : viewModel.getSuggestionsList()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Suggestion.fxml"));
                Node cardNode = loader.load();
                SuggestionController cardController = loader.getController();

                cardController.setData(suggestion, this);
                sugestoesContainerVBox.getChildren().add(cardNode);

            } catch (IOException e) {
                logger.error("Failed to load /fxml/Suggestion.fxml", e);
                e.printStackTrace();
            }
        }
    }

    // --- MÉTODOS DE HANDLER CHAMADOS PELO FXML ---
    @FXML
    private void handleRefreshAllButton() {
        logger.info("Refreshing suggestions...");
        viewModel.refreshAllSuggestions();
    }

    public void refreshSingleSuggestion(Suggestion suggestionToReplace) {
        logger.info("View received request to refresh single suggestion: {}",
                suggestionToReplace.destinationProperty().get());
        viewModel.refreshSingleSuggestion(suggestionToReplace);
    }

    @FXML
    private void handleBackButton() {
        logger.info("Home button clicked");
        try {
            Parent homePageRoot = FXMLLoader.load(Objects.requireNonNull(
                    getClass().getResource("/fxml/HomePage.fxml")));
            Stage stage = (Stage) sugestoesContainerVBox.getScene().getWindow();
            stage.getScene().setRoot(homePageRoot);
        } catch (IOException | NullPointerException e) {
            logger.error("Failed to load HomePage.fxml", e);
            showError("Could not load the home page.");
        }
    }

    @FXML
    private void openPreferences() {
        logger.info("Opening preferences...");

        try {
            Parent preferencesRoot = FXMLLoader.load(Objects.requireNonNull(
                    getClass().getResource("/fxml/Preferences.fxml")));

            Stage stage = (Stage) sugestoesContainerVBox.getScene().getWindow();
            Scene scene = stage.getScene();

            // Trocar o conteúdo da cena
            scene.setRoot(preferencesRoot);

            logger.info("Preferences page loaded successfully.");

        } catch (IOException | NullPointerException e) {
            logger.error("Failed to load Preferences.fxml", e);
            showError("Could not load the preferences page.");
        }
    }

    // --- Helpers ---
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void searchFlight(String cityName) {
        try {
            viewModel.setFlightSearch(cityName);
            viewModel.setLastPage();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/BasicFlightSearch.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root, SkySystemApp.WINDOW_WIDTH, SkySystemApp.WINDOW_HEIGHT);
            stage.setScene(scene);
            stage.setTitle(SkySystemApp.APP_TITLE);

            logger.info("Navigated to Flight Search");
            viewModel.setFlightSearch(cityName);
            viewModel.setLastPage();

        } catch (IOException e) {
            logger.error("Failed to navigate to Recommender", e);
            showError("Failed to return to Recommender: " + e.getMessage());
        }

    }

}