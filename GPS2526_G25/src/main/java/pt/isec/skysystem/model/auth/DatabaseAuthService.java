package pt.isec.skysystem.model.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.isec.skysystem.model.data.UserProfile;
import pt.isec.skysystem.model.data.dao.UserDAO;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Authentication Service using SQLite database
 */
public class DatabaseAuthService implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseAuthService.class);
    private final UserDAO userDAO;
    UserProfile currentUser;

    public DatabaseAuthService() {
        this.userDAO = new UserDAO();
        logger.info("DatabaseAuthService initialized");
    }

    @Override
    public boolean register(String username, String password, String email) {
        if (userDAO.emailExists(email)) {
            logger.warn("Registration failed: Email '{}' already exists", email);
            return false;
        }
        
        String passwordHash = hashPassword(password);
        if (passwordHash == null) {
            logger.error("Registration failed: Could not hash password");
            return false;
        }

        long userId = userDAO.createUser(username, email, passwordHash);
        if (userId != -1) {
            currentUser = new UserProfile(username, email, false);
            logger.info("User registered successfully: {} ({})", username, email);
            return true;
        } else {
            logger.error("Registration failed: Database error");
            return false;
        }
    }

    @Override
    public String login(String email, String password) {
        String passwordHash = hashPassword(password);
        if (passwordHash == null) {
            return null;
        }

        String username = userDAO.authenticate(email, passwordHash);
        if (username != null) {
            long userId = userDAO.getId(email);
            boolean onboardingCompleted = userDAO.isOnboardingCompleted(userId);
            currentUser = new UserProfile(username, email, onboardingCompleted);

            logger.info("User logged in successfully: {}", username);
            return username;
        } else {
            logger.warn("Login failed: Invalid credentials for email '{}'", email);
            return null;
        }
    }

    @Override
    public boolean userExists(String email) {
        return userDAO.emailExists(email);
    }

    public boolean updateOnboardingStatus(boolean completed) {
        if (currentUser == null) {
            return false;
        }
        long userId = userDAO.getId(currentUser.getEmail());
        if (userId != -1) {
            boolean success = userDAO.updateOnboardingStatus(userId, completed);
            if (success) {
                currentUser.setOnboardingCompleted(completed);
            }
            return success;
        }
        return false;
    }

    /**
     * Hashes the password using SHA-256
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            logger.error("Error hashing password", e);
            return null;
        }
    }

    //getter for current user

    public String getCurrentUsername() {return currentUser.getUsername();}

    public String getCurrentUserEmail() {return currentUser.getEmail();}

    public long getCurrentUserId() {return userDAO.getId(currentUser.getEmail());}

    public boolean getOnboardingStatus() {
        if (currentUser == null) {
            return false;
        }
        return userDAO.isOnboardingCompleted(userDAO.getId(currentUser.getEmail()));
    }
}
