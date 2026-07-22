package pt.isec.skysystem.model.data.dao;


import pt.isec.skysystem.model.data.DataBaseManager;

import java.sql.*;

public class UserDAO {

    /**
     * Creates a new user in the database.
     * @param username The username of the user.
     * @param email The email of the user.
     * @param passwordHash The hashed password of the user.
     * @return The ID of the newly created user, or -1 in case of error.
     */
    public long createUser(String username, String email, String passwordHash) {
        String sql = "INSERT INTO USER (username, email, password_hash, onboarding_completed) VALUES (?, ?, ?, 0)";
        long userId = -1;

        try (Connection conn = DataBaseManager.getConnection()) {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                pstmt.setString(2, email);
                pstmt.setString(3, passwordHash);

                int affectedRows = pstmt.executeUpdate();

                if (affectedRows > 0) {
                    try (Statement stmt = conn.createStatement();
                         ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()")) {
                        if (rs.next()) {
                            userId = rs.getLong(1);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error creating user: " + e.getMessage());
        }
        return userId;
    }

    /**
     * Authenticates a user by email and password hash.
     * @param email The user's email.
     * @param passwordHash The hashed password.
     * @return The username if authentication is successful, null otherwise.
     */
    public String authenticate(String email, String passwordHash) {
        String sql = "SELECT username FROM USER WHERE email = ? AND password_hash = ?";
        try (Connection conn = DataBaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            pstmt.setString(2, passwordHash);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("username");
                }
            }
        } catch (Exception e) {
            System.err.println("Error authenticating user: " + e.getMessage());
        }
        return null;
    }

    /**
     * Checks if an email already exists in the database.
     * @param email The email to check.
     * @return true if the email exists, false otherwise.
     */
    public boolean emailExists(String email) {
        String sql = "SELECT 1 FROM USER WHERE email = ?";
        try (Connection conn = DataBaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            System.err.println("Error checking email existence: " + e.getMessage());
        }
        return false;
    }

    /**
     * Checks if a username already exists in the database.
     * @param username The username to check.
     * @return true if the username exists, false otherwise.
     */
    public boolean usernameExists(String username) {
        String sql = "SELECT 1 FROM USER WHERE username = ?";
        try (Connection conn = DataBaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            System.err.println("Error checking username existence: " + e.getMessage());
        }
        return false;
    }

    public long getId(String email) {
        String sql = "SELECT user_id FROM USER WHERE email = ?";
        long userId = -1;

        try (Connection conn = DataBaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // O user_id é uma chave primária INTEGER
                    userId = rs.getLong("user_id");
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao obter user ID por email: " + e.getMessage());
        }
        return userId;
    }

    /**
     * Updates the onboarding status of a user.
     * @param userId The ID of the user.
     * @param completed The new status.
     * @return true if successful, false otherwise.
     */
    public boolean updateOnboardingStatus(long userId, boolean completed) {
        String sql = "UPDATE USER SET onboarding_completed = ? WHERE user_id = ?";
        try (Connection conn = DataBaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, completed);
            pstmt.setLong(2, userId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (Exception e) {
            System.err.println("Error updating onboarding status for user " + userId + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks if a user has completed the onboarding.
     * @param userId The ID of the user.
     * @return true if completed, false otherwise.
     */
    public boolean isOnboardingCompleted(long userId) {
        String sql = "SELECT onboarding_completed FROM USER WHERE user_id = ?";
        try (Connection conn = DataBaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("onboarding_completed");
                }
            }
        } catch (Exception e) {
            System.err.println("Error checking onboarding status for user " + userId + ": " + e.getMessage());
        }
        return false;
    }

    public String getNomeUser(long userId) {
        String sql = "SELECT username FROM USER WHERE user_id = ?";

        try (Connection conn = DataBaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // 1. Substitui o ? pelo ID recebido
            pstmt.setLong(1, userId);

            // 2. Executa a query
            try (ResultSet rs = pstmt.executeQuery()) {
                // 3. Se houver resultado, devolve o nome
                if (rs.next()) {
                    return rs.getString("username");
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao obter username pelo ID: " + e.getMessage());
        }

        return null; // Retorna null se não encontrar ou der erro
    }
}