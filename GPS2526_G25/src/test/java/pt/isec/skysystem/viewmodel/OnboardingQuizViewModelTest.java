package pt.isec.skysystem.viewmodel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pt.isec.skysystem.model.data.TravelPreferences;

import static org.junit.jupiter.api.Assertions.*;

class OnboardingQuizViewModelTest {

    private OnboardingQuizViewModel viewModel;

    @BeforeEach
    void setUp() {
        viewModel = new OnboardingQuizViewModel();
    }

    @Test
    @DisplayName("Deve iniciar no passo 1")
    void testInitialState() {
        assertEquals(1, viewModel.getCurrentStep());
        assertFalse(viewModel.isCanGoNext());
        assertFalse(viewModel.isCanGoPrevious());
        assertEquals(0.0, viewModel.getProgressPercentage());
    }

    @Test
    @DisplayName("Deve permitir selecionar Vibe e avançar")
    void testSelectVibe() {
        viewModel.selectVibe("Beach & Sun");
        assertEquals("Beach & Sun", viewModel.getSelectedVibe());
        
        assertTrue(viewModel.isCanGoNext());
        
        viewModel.nextStep();
        assertEquals(2, viewModel.getCurrentStep());
    }

    @Test
    @DisplayName("Deve permitir navegar entre passos")
    void testNavigation() {
        // Step 1
        viewModel.selectVibe("Beach"); 
        viewModel.nextStep();
        
        // Step 2
        assertEquals(2, viewModel.getCurrentStep());
        assertTrue(viewModel.isCanGoPrevious());
        
        viewModel.previousStep();
        assertEquals(1, viewModel.getCurrentStep());
    }
}