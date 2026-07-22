package pt.isec.skysystem.viewmodel;

import javafx.beans.property.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.isec.skysystem.model.DataFacade;
import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for the Home Page
 * Manages state and business logic
 */
public class HomePageViewModel {
    private static final Logger logger = LoggerFactory.getLogger(HomePageViewModel.class);

    private final DataFacade dataFacade;

    // Observable properties for View binding
    private final BooleanProperty needsOnboarding = new SimpleBooleanProperty(true);
    private final BooleanProperty aiAssistantEnabled = new SimpleBooleanProperty(false);
    private final StringProperty quizButtonText = new SimpleStringProperty("START QUIZ");
    private final StringProperty currentUsername = new SimpleStringProperty("");
    private final StringProperty heroGreeting = new SimpleStringProperty("Your Journey Starts Here.");

    public HomePageViewModel() {
        this.dataFacade = DataFacade.getInstance();
        updateUserStatus();
    }

    /**
     * Updates all properties based on current user status
     * Called on initialization and after quiz completion
     */
    public void updateUserStatus() {
        if (dataFacade.hasCurrentUser()) {

            boolean userNeedsOnboarding = dataFacade.currentUserNeedsOnboarding();

            needsOnboarding.set(userNeedsOnboarding);
            aiAssistantEnabled.set(!userNeedsOnboarding);
            quizButtonText.set(userNeedsOnboarding ? "START QUIZ" : "QUIZ");
            currentUsername.set(dataFacade.getCurrentUser().getUsername());
            heroGreeting.set(dataFacade.getCurrentUser().getUsername() + ", Your Journey Starts Here.");

            logger.info("User status updated - Needs onboarding: {}", userNeedsOnboarding);

            // Pre-load suggestions if user is ready and cache is empty
            if (hasCompletedOnboarding() && dataFacade.getLastAiSuggestions().isEmpty()) {
                logger.info("User has completed onboarding and has no cached suggestions. Pre-loading in background...");
                // Thread to pre-load the suggestions
                new Thread(() -> {
                    try {
                        dataFacade.getAiSuggestions(new ArrayList<>());
                        logger.info("AI suggestions pre-loaded successfully.");
                    } catch (Exception e) {
                        logger.error("Failed to pre-load AI suggestions in background", e);
                    }
                }).start();
            }
        } else {
            needsOnboarding.set(true);
            aiAssistantEnabled.set(false);
            quizButtonText.set("START QUIZ");
            currentUsername.set("");

            logger.warn("No current user found");
        }
    }

    /**
     * Gets AI recommendation for current user
     * Runs in background to avoid blocking UI
     *
     * @return JSONObject with recommendation, or null if error
     */
    public JSONObject getAIRecommendation() {
        if (dataFacade.currentUserNeedsOnboarding()) {
            logger.error("Cannot get AI recommendation - user needs onboarding");
            return null;
        }

        try {
            logger.info("Requesting AI recommendation...");
            JSONObject recommendation = dataFacade.getRecommendationForCurrentUser();
            logger.info("AI recommendation received successfully");
            return recommendation;

        } catch (Exception e) {
            logger.error("Failed to get AI recommendation", e);
            return null;
        }
    }

    /**
     * Gets user preferences summary
     */
    public String getPreferencesSummary() {
        if (!dataFacade.hasCurrentUser()) {
            return "No user logged in";
        }

        if (dataFacade.currentUserNeedsOnboarding()) {
            return "No preferences set. Please complete the quiz.";
        }

        return dataFacade.getCurrentUserPreferencesSummary();
    }

    /**
     * Gets current username
     */
    public String getCurrentUsername() {
        return dataFacade.hasCurrentUser() ? dataFacade.getCurrentUser().getUsername() : "Guest";
    }

    /**
     * Checks if user has completed onboarding
     */
    public boolean hasCompletedOnboarding() {
        return dataFacade.hasCurrentUser() &&
                !dataFacade.currentUserNeedsOnboarding();
    }

    /**
     * Gets a list of image URLs for the home page slideshow
     * Combines hardcoded quality images and generic scenic images
     * 
     * @return List of image URLs for the slideshow
     */
    public List<String> getSlideshowImageUrls() {
        List<String> urls = new ArrayList<>();

        // Add default quality images from constants
        urls.addAll(HomePageConstants.DEFAULT_SLIDESHOW_IMAGES);

        // Add generic scenic images with proper dimensions
        for (String searchTerm : HomePageConstants.SCENIC_SEARCH_TERMS) {
            String imageUrl = dataFacade.getValidImageUrl(searchTerm, HomePageConstants.SLIDESHOW_IMAGE_WIDTH, HomePageConstants.SLIDESHOW_IMAGE_HEIGHT);
            if (!urls.contains(imageUrl)) {
                urls.add(imageUrl);
            }
        }

        java.util.Collections.shuffle(urls);

        logger.info("Generated {} slideshow image URLs", urls.size());
        return urls;
    }

    // ===== PROPERTIES =====

    public BooleanProperty needsOnboardingProperty() {
        return needsOnboarding;
    }

    public boolean isNeedsOnboarding() {
        return needsOnboarding.get();
    }

    public BooleanProperty aiAssistantEnabledProperty() {
        return aiAssistantEnabled;
    }

    public boolean isAiAssistantEnabled() {
        return aiAssistantEnabled.get();
    }

    public void logout() {
        dataFacade.logout();
    }

    public StringProperty quizButtonTextProperty() {
        return quizButtonText;
    }

    public String getQuizButtonText() {
        return quizButtonText.get();
    }

    public StringProperty currentUsernameProperty() {
        return currentUsername;
    }

    public StringProperty heroGreetingProperty() {
        return heroGreeting;
    }

    public String getCurrentUsernameValue() {
        return currentUsername.get();
    }
}