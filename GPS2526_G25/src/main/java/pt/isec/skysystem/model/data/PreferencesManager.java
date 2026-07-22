package pt.isec.skysystem.model.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages user profiles and preferences with file persistence
 * Singleton pattern for global access
 */
public class PreferencesManager {
    private static final Logger logger = LoggerFactory.getLogger(PreferencesManager.class);
    private static PreferencesManager instance;


    private UserProfile currentUser;


    private PreferencesManager() {



        logger.info("PreferencesManager initialized");
    }

    public static PreferencesManager getInstance() {
        if (instance == null) {
            instance = new PreferencesManager();
        }
        return instance;
    }

    // ==================== USER MANAGEMENT ====================
    public void setCurrentUser(UserProfile user) {
        this.currentUser = user;
    }

    public UserProfile getCurrentUser() {
        return currentUser;
    }

    // ==================== PREFERENCES MANAGEMENT ====================
    public boolean savePreferences(TravelPreferences preferences) {
        if (currentUser == null) {
            logger.error("Cannot save preferences - no current user");
            return false;
        }

        if (preferences.isComplete()) {

            currentUser.completeOnboarding(preferences);
            logger.info("Preferences saved for user: {}", currentUser.getUsername());
            return true;
        }
        logger.warn("Cannot save incomplete preferences");
        return false;
    }

    public boolean currentUserNeedsOnboarding() {
        return currentUser == null || currentUser.needsOnboarding();
    }

    public void resetCurrentUserPreferences() {
        if (currentUser != null) {
            currentUser.setPreferences(new TravelPreferences());
            currentUser.setOnboardingCompleted(false);
            logger.info("Preferences reset for user: {}", currentUser.getUsername());
        }
    }

}