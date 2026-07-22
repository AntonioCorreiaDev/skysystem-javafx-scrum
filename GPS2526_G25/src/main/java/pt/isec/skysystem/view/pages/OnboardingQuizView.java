package pt.isec.skysystem.view.pages;

import pt.isec.skysystem.SkySystemApp;
import pt.isec.skysystem.viewmodel.OnboardingQuizViewModel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Controller for the onboarding quiz view
 */
public class OnboardingQuizView {
    private static final Logger logger = LoggerFactory.getLogger(OnboardingQuizView.class);

    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label progressLabel;
    @FXML
    private Label stepTitleLabel;

    @FXML
    private Button optionButton1;
    @FXML
    private Button optionButton2;
    @FXML
    private Button optionButton3;
    @FXML
    private Button optionButton4;
    @FXML
    private Button optionButton5;
    @FXML
    private Button optionButton6;
    private List<Button> optionButtons;

    @FXML
    private Button previousButton;
    @FXML
    private Button nextButton;
    @FXML
    private Button backButton;

    private OnboardingQuizViewModel viewModel;

    @FXML
    public void initialize() {
        this.viewModel = new OnboardingQuizViewModel();
        optionButtons = Arrays.asList(optionButton1, optionButton2, optionButton3,
                optionButton4, optionButton5, optionButton6);

        setupBindings();
        updateQuestionUI();

        logger.info("OnboardingQuizView initialized");
    }

    /**
     * Bind JavaFX properties to ViewModel
     */
    private void setupBindings() {
        progressBar.progressProperty().bind(viewModel.progressPercentageProperty());
        progressLabel.textProperty().bind(viewModel.progressTextProperty());

        previousButton.disableProperty().bind(viewModel.canGoPreviousProperty().not());
        nextButton.disableProperty().bind(viewModel.canGoNextProperty().not());

        viewModel.isLastStepProperty().addListener((obs, oldVal, newVal) -> {
            nextButton.setText(newVal ? "Complete" : "Next");
        });

        viewModel.currentStepProperty().addListener((obs, oldVal, newVal) -> {
            updateQuestionUI();
        });
    }

    /**
     * Updates UI based on current step
     */
    private void updateQuestionUI() {
        int step = viewModel.getCurrentStep();
        optionButtons.forEach(btn -> btn.setVisible(true));

        switch (step) {
            case 1:
                stepTitleLabel.setText("What's your ideal vacation vibe?");
                var vibeOptions = viewModel.getVibeOptions();
                setButtons(vibeOptions);
                updateSingleSelectVisual(viewModel.getSelectedVibe());
                break;

            case 2:
                stepTitleLabel.setText("What's your typical travel style?");
                var budgetOptions = viewModel.getBudgetOptions();
                setButtons(budgetOptions);
                updateSingleSelectVisual(viewModel.getSelectedBudget());
                break;

            case 3:
                stepTitleLabel.setText("What's your ideal vacation pace?");
                var paceOptions = viewModel.getPaceOptions();
                setButtons(paceOptions);
                updateSingleSelectVisual(viewModel.getSelectedPace());
                break;

            case 4:
                stepTitleLabel.setText("Who do you usually travel with?");
                var companyOptions = viewModel.getCompanyOptions();
                setButtons(companyOptions);
                updateSingleSelectVisual(viewModel.getSelectedCompany());
                break;

            case 5:
                stepTitleLabel.setText("What's your preferred climate?");
                var climateOptions = viewModel.getClimateOptions();
                setButtons(climateOptions);
                updateSingleSelectVisual(viewModel.getSelectedClimate());
                break;
        }
    }

    private void setButtons(List<OnboardingQuizViewModel.OptionData> options) {
        for (int i = 0; i < optionButtons.size(); i++) {
            if (i < options.size()) {
                optionButtons.get(i).setText(options.get(i).displayName());
                optionButtons.get(i).setUserData(options.get(i).value());
                optionButtons.get(i).setVisible(true);
            } else {
                optionButtons.get(i).setVisible(false);
            }
        }
    }

    @FXML
    private void handleOption1() {
        handleSelection(0);
    }

    @FXML
    private void handleOption2() {
        handleSelection(1);
    }

    @FXML
    private void handleOption3() {
        handleSelection(2);
    }

    @FXML
    private void handleOption4() {
        handleSelection(3);
    }

    @FXML
    private void handleOption5() {
        handleSelection(4);
    }

    @FXML
    private void handleOption6() {
        handleSelection(5);
    }

    private void handleSelection(int index) {
        Button clickedButton = optionButtons.get(index);
        String selectedValue = (String) clickedButton.getUserData();

        if (selectedValue == null)
            return;

        int currentStep = viewModel.getCurrentStep();
        if (currentStep == 1) {
            viewModel.selectVibe(selectedValue);
            updateSingleSelectVisual(selectedValue);
        } else {
            switch (currentStep) {
                case 2 -> viewModel.selectBudget(selectedValue);
                case 3 -> viewModel.selectPace(selectedValue);
                case 4 -> viewModel.selectCompany(selectedValue);
                case 5 -> viewModel.selectClimate(selectedValue);
            }
            updateSingleSelectVisual(selectedValue);
        }
    }

    private void updateSingleSelectVisual(String selectedValue) {
        for (Button btn : optionButtons) {
            btn.getStyleClass().remove("selected");
        }

        if (selectedValue != null) {
            for (Button btn : optionButtons) {
                if (selectedValue.equals(btn.getUserData())) {
                    if (!btn.getStyleClass().contains("selected")) {
                        btn.getStyleClass().add("selected");
                    }
                    break;
                }
            }
        }
    }

    @FXML
    private void handlePrevious() {
        viewModel.previousStep();
    }

    @FXML
    private void handleNext() {
        if (viewModel.isLastStep()) {
            handleComplete();
        } else {
            viewModel.nextStep();
        }
    }

    private void handleComplete() {
        boolean success = viewModel.completeQuiz();

        if (success) {
            logger.info("Quiz completed successfully! Navigating to Recommender...");
            navigateToRecommender();

        } else {
            showError("Please answer all questions before completing the quiz.");
        }
    }

    private void navigateToRecommender(){
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/ai_suggestions.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root, SkySystemApp.WINDOW_WIDTH, SkySystemApp.WINDOW_HEIGHT);
            stage.setScene(scene);
            stage.setTitle(SkySystemApp.APP_TITLE);

            logger.info("Navigated to Recommender");

        } catch (IOException e) {
            logger.error("Failed to navigate to Recommender", e);
            showError("Failed to return to Recommender: " + e.getMessage());
        }
    }

    private void navigateToHomePage() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/HomePage.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root, SkySystemApp.WINDOW_WIDTH, SkySystemApp.WINDOW_HEIGHT);
            stage.setScene(scene);
            stage.setTitle(SkySystemApp.APP_TITLE);

            logger.info("Navigated to HomePage");

        } catch (IOException e) {
            logger.error("Failed to navigate to HomePage", e);
            showError("Failed to return to home page: " + e.getMessage());
        }
    }

    @FXML
    private void handleBackButton() {
        navigateToHomePage();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}