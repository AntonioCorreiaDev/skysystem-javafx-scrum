package pt.isec.skysystem.model.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TravelPreferencesTest {

    private TravelPreferences preferences;

    @BeforeEach
    void setUp() {
        preferences = new TravelPreferences();
    }

    @Test
    @DisplayName("Deve inicializar vazio (incomplete)")
    void testInitialization() {
        assertNull(preferences.getVibe());
        assertNull(preferences.getBudgetLevel());
        assertNull(preferences.getPace());
        assertNull(preferences.getCompany());
        assertNull(preferences.getClimate());
        assertFalse(preferences.isComplete());
    }

    @Test
    @DisplayName("Deve definir e recuperar Vibe (Single Select)")
    void testSetVibe() {
        preferences.setVibe(TravelPreferences.Vibe.BEACH_SUN);
        assertEquals(TravelPreferences.Vibe.BEACH_SUN, preferences.getVibe());

        // Change choice
        preferences.setVibe(TravelPreferences.Vibe.MOUNTAIN_ADVENTURE);
        assertEquals(TravelPreferences.Vibe.MOUNTAIN_ADVENTURE, preferences.getVibe());
    }

    @Test
    @DisplayName("Deve verificar se está completo corretamente")
    void testIsComplete() {
        assertFalse(preferences.isComplete());

        preferences.setVibe(TravelPreferences.Vibe.CITY_CULTURE);
        assertFalse(preferences.isComplete());

        preferences.setBudgetLevel(TravelPreferences.BudgetLevel.MID_RANGE);
        assertFalse(preferences.isComplete());

        preferences.setPace(TravelPreferences.TravelPace.BALANCED);
        assertFalse(preferences.isComplete());

        preferences.setCompany(TravelPreferences.TravelCompany.FRIENDS);
        assertFalse(preferences.isComplete());

        preferences.setClimate(TravelPreferences.ClimatePreference.WARM_MILD);
        assertTrue(preferences.isComplete());
    }

    @Test
    @DisplayName("Deve recuperar todas as tags corretamente")
    void testGetAllTags() {
        preferences.setVibe(TravelPreferences.Vibe.BEACH_SUN); 
        preferences.setBudgetLevel(TravelPreferences.BudgetLevel.LOW_END);
        preferences.setPace(TravelPreferences.TravelPace.RELAXED);
        preferences.setCompany(TravelPreferences.TravelCompany.SOLO);
        preferences.setClimate(TravelPreferences.ClimatePreference.HOT_SUNNY);

        List<String> tags = preferences.getAllTags();

        // Check if contains tags from all categories
        assertTrue(tags.contains("beach")); // From Vibe
        assertTrue(tags.contains("budget_low")); // From Budget
        assertTrue(tags.contains("style_relaxed")); // From Pace
        assertTrue(tags.contains("solo")); // From Company
        assertTrue(tags.contains("climate_hot")); // From Climate
    }

    @Test
    @DisplayName("Enum Vibe deve retornar UI strings corretas")
    void testEnumStrings() {
        assertEquals("Beach", TravelPreferences.Vibe.BEACH_SUN.toUI());
        assertEquals("Mountain", TravelPreferences.Vibe.MOUNTAIN_ADVENTURE.toUI());
    }
}