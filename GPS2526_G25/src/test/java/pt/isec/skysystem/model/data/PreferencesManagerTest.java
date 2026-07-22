package pt.isec.skysystem.model.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PreferencesManagerTest {

    private PreferencesManager manager;

    @BeforeEach
    void setUp() {
        manager = PreferencesManager.getInstance();
        manager.setCurrentUser(null); // Reset state
    }

    @Test
    @DisplayName("Deve gerenciar o utilizador atual corretamente")
    void testCurrentUser() {
        assertNull(manager.getCurrentUser());

        UserProfile user = new UserProfile("testUser", "test@email.com", false);
        manager.setCurrentUser(user);

        assertNotNull(manager.getCurrentUser());
        assertEquals("testUser", manager.getCurrentUser().getUsername());
    }

    @Test
    @DisplayName("Deve saber se precisa de onboarding")
    void testNeedsOnboarding() {
        // No user -> needs onboarding (or logic says true if null)
        assertTrue(manager.currentUserNeedsOnboarding());

        UserProfile user = new UserProfile("testUser", "test@email.com", false);
        manager.setCurrentUser(user);

        // User created with onboardingCompleted = false
        assertTrue(manager.currentUserNeedsOnboarding());

        TravelPreferences prefs = new TravelPreferences();
        // Complete
        prefs.setVibe(TravelPreferences.Vibe.BEACH_SUN);
        prefs.setBudgetLevel(TravelPreferences.BudgetLevel.LOW_END);
        prefs.setPace(TravelPreferences.TravelPace.RELAXED);
        prefs.setCompany(TravelPreferences.TravelCompany.SOLO);
        prefs.setClimate(TravelPreferences.ClimatePreference.HOT_SUNNY);
        manager.savePreferences(prefs);

        user.setOnboardingCompleted(true);

        assertFalse(manager.currentUserNeedsOnboarding());
    }

    @Test
    @DisplayName("Deve salvar preferências no user atual em memória")
    void testSavePreferences() {
        UserProfile user = new UserProfile("testUser", "test@email.com", false);
        manager.setCurrentUser(user);

        TravelPreferences prefs = new TravelPreferences();
        // Incomplete -> fail
        boolean result = manager.savePreferences(prefs);
        assertFalse(result);

        // Complete
        prefs.setVibe(TravelPreferences.Vibe.BEACH_SUN);
        prefs.setBudgetLevel(TravelPreferences.BudgetLevel.LOW_END);
        prefs.setPace(TravelPreferences.TravelPace.RELAXED);
        prefs.setCompany(TravelPreferences.TravelCompany.SOLO);
        prefs.setClimate(TravelPreferences.ClimatePreference.HOT_SUNNY);

        result = manager.savePreferences(prefs);
        assertTrue(result);

        // Verify user state updated
        assertTrue(user.isOnboardingCompleted());
        assertNotNull(user.getPreferences());
        assertEquals(TravelPreferences.Vibe.BEACH_SUN, user.getPreferences().getVibe());
    }

    @Test
    @DisplayName("Deve resetar preferências")
    void testResetPreferences() {
        UserProfile user = new UserProfile("testUser", "test@email.com", true);
        manager.setCurrentUser(user);

        manager.resetCurrentUserPreferences();

        assertFalse(user.isOnboardingCompleted());
        assertFalse(user.getPreferences().isComplete());
    }
}