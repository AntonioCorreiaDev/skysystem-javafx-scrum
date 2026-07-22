package pt.isec.skysystem.viewmodel;

import javafx.beans.property.*;
import pt.isec.skysystem.model.DataFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * ViewModel for the onboarding quiz
 * Manages the 5-step quiz flow and user selections
 * Works only with primitive data (Strings) - no direct access to model classes
 */
public class OnboardingQuizViewModel {
    private static final Logger logger = LoggerFactory.getLogger(OnboardingQuizViewModel.class);
    private static final int TOTAL_STEPS = 5;

    public record OptionData(String value, String displayName) {}

    private final IntegerProperty currentStep = new SimpleIntegerProperty(1);

    private final String pageName="OnboardingQuiz";

    // Quiz answers (using enum names as Strings)
    private String selectedVibe = null;
    private String selectedBudget = null;
    private String selectedPace = null;
    private String selectedCompany = null;
    private String selectedClimate = null;

    // UI state
    private final BooleanProperty canGoNext = new SimpleBooleanProperty(false);
    private final BooleanProperty canGoPrevious = new SimpleBooleanProperty(false);
    private final BooleanProperty isLastStep = new SimpleBooleanProperty(false);
    private final StringProperty progressText = new SimpleStringProperty("1 of 5");
    private final DoubleProperty progressPercentage = new SimpleDoubleProperty(0.0);

    private final DataFacade dataFacade;

    public OnboardingQuizViewModel() {
        this.dataFacade = DataFacade.getInstance();

        currentStep.addListener((obs, oldVal, newVal) -> updateUIState());
        updateUIState();
    }

    public List<OptionData> getVibeOptions() {
        return dataFacade.getVibeOptions();
    }

    public List<OptionData> getBudgetOptions() {
        return dataFacade.getBudgetOptions();
    }

    public List<OptionData> getPaceOptions() {
        return dataFacade.getPaceOptions();
    }

    public List<OptionData> getCompanyOptions() {
        return dataFacade.getCompanyOptions();
    }

    public List<OptionData> getClimateOptions() {
        return dataFacade.getClimateOptions();
    }

    public void nextStep() {
        if (currentStep.get() < TOTAL_STEPS) {
            currentStep.set(currentStep.get() + 1);
            logger.info("Moved to step: {}", currentStep.get());
        }
    }

    public void previousStep() {
        if (currentStep.get() > 1) {
            currentStep.set(currentStep.get() - 1);
            logger.info("Moved to step: {}", currentStep.get());
        }
    }

    public void goToStep(int step) {
        if (step >= 1 && step <= TOTAL_STEPS) {
            currentStep.set(step);
        }
    }

    // ==================== QUESTION 1: VIBES (Single-select) ====================

    public void selectVibe(String vibeName) {
        selectedVibe = vibeName;
        logger.info("Selected vibe: {}", vibeName);
        updateUIState();
    }

    public String getSelectedVibe() {
        return selectedVibe;
    }

    // ==================== QUESTION 2: BUDGET (Single-select) ====================

    public void selectBudget(String budgetName) {
        selectedBudget = budgetName;
        logger.info("Selected budget: {}", budgetName);
        updateUIState();
    }

    public String getSelectedBudget() {
        return selectedBudget;
    }

    // ==================== QUESTION 3: PACE (Single-select) ====================

    public void selectPace(String paceName) {
        selectedPace = paceName;
        logger.info("Selected pace: {}", paceName);
        updateUIState();
    }

    public String getSelectedPace() {
        return selectedPace;
    }

    // ==================== QUESTION 4: COMPANY (Single-select) ====================

    public void selectCompany(String companyName) {
        selectedCompany = companyName;
        logger.info("Selected company: {}", companyName);
        updateUIState();
    }

    public String getSelectedCompany() {
        return selectedCompany;
    }

    // ==================== QUESTION 5: CLIMATE (Single-select) ====================

    public void selectClimate(String climateName) {
        selectedClimate = climateName;
        logger.info("Selected climate: {}", climateName);
        updateUIState();
    }

    public String getSelectedClimate() {
        return selectedClimate;
    }

    // ==================== QUIZ COMPLETION ====================

    /**
     * Completes the quiz by sending raw data to DataFacade
     */
    public boolean completeQuiz() {
        if (!isQuizComplete()) {
            logger.error("Cannot complete quiz - not all questions answered");
            return false;
        }

        try {
            // Pass raw String data to facade
            boolean success = dataFacade.completeOnboarding(
                    selectedVibe,
                    selectedBudget,
                    selectedPace,
                    selectedCompany,
                    selectedClimate
            );

            if (success) {
                dataFacade.updateOnboardingStatus(true);
                progressPercentage.set(1.0);
                logger.info("Quiz completed successfully");
            } else {
                logger.error("Failed to save quiz preferences");
            }

            return success;

        } catch (Exception e) {
            logger.error("Error completing quiz", e);
            return false;
        }
    }

    /**
     * Checks if all quiz questions have been answered
     */
    public boolean isQuizComplete() {
        return selectedVibe != null &&
                selectedBudget != null &&
                selectedPace != null &&
                selectedCompany != null &&
                selectedClimate != null;
    }

    /**
     * Resets all quiz answers
     */
    public void resetQuiz() {
        selectedVibe = null;
        selectedBudget = null;
        selectedPace = null;
        selectedCompany = null;
        selectedClimate = null;
        currentStep.set(1);
        logger.info("Quiz reset");
    }

    // ==================== UI STATE MANAGEMENT ====================

    private void updateUIState() {
        int step = currentStep.get();

        canGoPrevious.set(step > 1);
        isLastStep.set(step == TOTAL_STEPS);

        boolean currentStepAnswered = isCurrentStepAnswered();
        canGoNext.set(currentStepAnswered);

        progressText.set(step + " of " + TOTAL_STEPS);
        progressPercentage.set((double) (step - 1) / TOTAL_STEPS);
    }



    private boolean isCurrentStepAnswered() {
        return switch (currentStep.get()) {
            case 1 -> selectedVibe != null;
            case 2 -> selectedBudget != null;
            case 3 -> selectedPace != null;
            case 4 -> selectedCompany != null;
            case 5 -> selectedClimate != null;
            default -> false;
        };
    }

    // ==================== PROPERTIES ====================

    public IntegerProperty currentStepProperty() {
        return currentStep;
    }

    public int getCurrentStep() {
        return currentStep.get();
    }

    public BooleanProperty canGoNextProperty() {
        return canGoNext;
    }

    public boolean isCanGoNext() {
        return canGoNext.get();
    }

    public BooleanProperty canGoPreviousProperty() {
        return canGoPrevious;
    }

    public boolean isCanGoPrevious() {
        return canGoPrevious.get();
    }

    public BooleanProperty isLastStepProperty() {
        return isLastStep;
    }

    public boolean isLastStep() {
        return isLastStep.get();
    }

    public StringProperty progressTextProperty() {
        return progressText;
    }

    public String getProgressText() {
        return progressText.get();
    }

    public DoubleProperty progressPercentageProperty() {
        return progressPercentage;
    }

    public double getProgressPercentage() {
        return progressPercentage.get();
    }
}