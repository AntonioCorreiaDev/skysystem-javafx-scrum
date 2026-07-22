package pt.isec.skysystem.model.data.dao;

import pt.isec.skysystem.model.data.DataBaseManager;
import pt.isec.skysystem.model.data.TravelPreferences;
import pt.isec.skysystem.model.data.TravelPreferences.Vibe;
import pt.isec.skysystem.model.data.TravelPreferences.BudgetLevel;
import pt.isec.skysystem.model.data.TravelPreferences.TravelPace;
import pt.isec.skysystem.model.data.TravelPreferences.TravelCompany;
import pt.isec.skysystem.model.data.TravelPreferences.ClimatePreference;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TravelPreferencesDAO {

    /**
     * Saves or updates travel preferences for a user.
     * Uses INSERT OR REPLACE because the PRIMARY KEY is user_id.
     * @param userId The user ID.
     * @param preferences The complete TravelPreferences object.
     * @return true if the operation was successful.
     */
    public boolean savePreferences(long userId, TravelPreferences preferences) {

        String sql = "INSERT OR REPLACE INTO TRAVELPREFERENCES (" +
                "user_id, vibe, budget_level, pace, company, climate" +
                ") VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DataBaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);
            // Os Enums são guardados na BD como strings (o seu nome)
            pstmt.setString(2, preferences.getVibe().name());
            pstmt.setString(3, preferences.getBudgetLevel().name());
            pstmt.setString(4, preferences.getPace().name());
            pstmt.setString(5, preferences.getCompany().name());
            pstmt.setString(6, preferences.getClimate().name());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (Exception e) {
            System.err.println("Error saving/updating preferences: " + e.getMessage());
            return false;
        }
    }

    /**
     * Loads travel preferences for a user.
     * This is the method that UserDAO should call to load the complete profile.
     * @param userId The user ID.
     * @return The TravelPreferences object if found, or null.
     */
    public TravelPreferences loadPreferencesByUserId(long userId) {
        String sql = "SELECT vibe, budget_level, pace, company, climate FROM TRAVELPREFERENCES WHERE user_id = ?";

        try (Connection conn = DataBaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    TravelPreferences preferences = new TravelPreferences();

                    // Mapear strings da BD de volta para os Enums Java usando valueOf()
                    preferences.setVibe(Vibe.valueOf(rs.getString("vibe")));
                    preferences.setBudgetLevel(BudgetLevel.valueOf(rs.getString("budget_level")));
                    preferences.setPace(TravelPace.valueOf(rs.getString("pace")));
                    preferences.setCompany(TravelCompany.valueOf(rs.getString("company")));
                    preferences.setClimate(ClimatePreference.valueOf(rs.getString("climate")));

                    return preferences;
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading user preferences " + userId + ": " + e.getMessage());
        }
        return null;
    }

    /**
     * Deletes all preferences for a user from the TRAVELPREFERENCES table.
     * @param userId The user ID whose preferences should be deleted.
     * @return true if the operation was successful.
     */
    public boolean deletePreferencesByUserId(long userId) {
        String sql = "DELETE FROM TRAVELPREFERENCES WHERE user_id = ?";

        try (Connection conn = DataBaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);
            pstmt.executeUpdate();

            return true;

        } catch (Exception e) {
            System.err.println("Error deleting user preferences " + userId + ": " + e.getMessage());
            return false;
        }
    }
}
