package pt.isec.skysystem.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pt.isec.skysystem.model.data.DataBaseManager;
import pt.isec.skysystem.model.data.PreferencesManager;
import pt.isec.skysystem.model.data.TravelPreferences;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DataFacadeTest {

    private DataFacade facade;

    @BeforeEach
    void setUp() throws Exception {
        // 1. Reset Singleton DataFacade
        resetSingleton(DataFacade.class, "instance");

        // 2. Reset Singleton PreferencesManager
        resetSingleton(PreferencesManager.class, "instance");

        // 3. Set Test Mode
        DataBaseManager.setTestMode(true);

        // 4. Initialize Facade
        facade = DataFacade.getInstance();
    }

    @AfterEach
    void tearDown() {
        try {
            Files.deleteIfExists(Paths.get("database/skysystem_test.db"));
            Files.deleteIfExists(Paths.get("database/skysystem_test.db-journal"));
        } catch (Exception e) {
            // ignore
        }
    }

    @Test
    @DisplayName("Singleton: Should always return same instance")
    void testSingleton() {
        DataFacade instance1 = DataFacade.getInstance();
        DataFacade instance2 = DataFacade.getInstance();
        assertSame(instance1, instance2);
    }

    @Test
    @DisplayName("Session: Should manage current user")
    void testUserSession() {
        assertFalse(facade.hasCurrentUser());

        facade.register("TestUser", "pass", "test@test.com");
        facade.setCurrentUser("TestUser", "test@test.com");
        
        assertTrue(facade.hasCurrentUser());
        assertEquals("TestUser", facade.getCurrentUser().getUsername());
        
        facade.logout();
        assertFalse(facade.hasCurrentUser());
    }

    @Test
    @DisplayName("Custom Tags: Should manage custom tags")
    void testCustomTagsManagement() {
        assertFalse(facade.hasCustomTags());
        
        List<String> tags = List.of("beach", "summer");
        facade.setCustomTags(tags);
        
        assertTrue(facade.hasCustomTags());
        assertEquals(2, facade.getCustomTags().size());
        
        facade.clearCustomTags();
        assertFalse(facade.hasCustomTags());
    }

    @Test
    @DisplayName("Onboarding: Should fail if no user logged in")
    void testCompleteOnboardingNoUser() {
        boolean result = facade.completeOnboarding(
            "BEACH_SUN", "MID_RANGE", "BALANCED", "FRIENDS", "WARM_MILD"
        );
        assertFalse(result, "Should fail without user");
    }
    
    @Test
    @DisplayName("Onboarding: Should succeed with valid data and user")
    void testCompleteOnboardingSuccess() {
        // 1. Setup User
        facade.register("TagUser", "pass", "tag@test.com");
        facade.setCurrentUser("TagUser", "tag@test.com");
        
        assertTrue(facade.hasCurrentUser());
        
        // 2. Complete Onboarding
        boolean result = facade.completeOnboarding(
             "BEACH_SUN", 
             "MID_RANGE", 
             "BALANCED", 
             "FRIENDS", 
             "WARM_MILD"
        );
        
        assertTrue(result, "Onboarding should succeed");
        assertNotNull(facade.getCurrentUserPreferences());
        assertEquals(TravelPreferences.Vibe.BEACH_SUN, facade.getCurrentUserPreferences().getVibe());
    }

    // --- HELPER ---
    private void resetSingleton(Class<?> clazz, String fieldName) throws Exception {
        Field instance = clazz.getDeclaredField(fieldName);
        instance.setAccessible(true);
        instance.set(null, null);
    }
}