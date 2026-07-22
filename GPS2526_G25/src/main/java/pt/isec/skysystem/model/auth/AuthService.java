package pt.isec.skysystem.model.auth;

/**
 * Interface for Authentication Services
 * Defines the contract for user registration and login
 */
public interface AuthService {
    
    /**
     * Registers a new user
     * @param username The desired username
     * @param password The password
     * @param email The email address
     * @return true if registration was successful, false otherwise
     */
    boolean register(String username, String password, String email);

    /**
     * Authenticates a user
     * @param email The email address
     * @param password The password
     * @return The username if credentials are valid, null otherwise
     */
    String login(String email, String password);

    /**
     * Checks if a user already exists
     * @param email The email to check
     * @return true if the user exists
     */
    boolean userExists(String email);

    //getter for current user
    String getCurrentUsername();

    String getCurrentUserEmail();

    long getCurrentUserId();

    /**
     * Updates the onboarding status of the current user.
     * @param completed The new status.
     * @return true if successful, false otherwise.
     */
    boolean updateOnboardingStatus(boolean completed);

    boolean getOnboardingStatus();
}
