package pt.isec.skysystem.model.data;

import com.google.genai.Client;
import com.google.genai.Models;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.GenerateContentConfig;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AIRecommenderTest {

    private AIRecommender aiRecommender;

    @Mock
    private Client mockClient;

    @Mock
    private Models mockModels;

    @Mock
    private TravelPreferences mockPreferences;

    @Mock
    private GenerateContentResponse mockResponse;

    @BeforeEach
    void setUp() throws Exception {
        aiRecommender = new AIRecommender();

        // injetar o mock atraves de uma reflexao
        Field clientField = AIRecommender.class.getDeclaredField("client");
        clientField.setAccessible(true);
        clientField.set(aiRecommender, mockClient);

        Field modelsField = Client.class.getDeclaredField("models");
        modelsField.setAccessible(true);
        modelsField.set(mockClient, mockModels);
    }

    @Test
    @DisplayName("Deve lançar exceção se as Preferências forem null")
    void testGetMultipleRecommendations_NullPreferences() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            aiRecommender.getMultipleRecommendations(null, 3);
        });
        assertEquals("TravelPreferences cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção se as Preferências estiverem incompletas")
    void testGetMultipleRecommendations_IncompletePreferences() {
        // simula preferencia incompleta
        when(mockPreferences.isComplete()).thenReturn(false);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            aiRecommender.getMultipleRecommendations(mockPreferences, 3);
        });
        assertEquals("TravelPreferences must be complete", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção se o count for menor que 1")
    void testGetMultipleRecommendations_CountTooLow() {
        when(mockPreferences.isComplete()).thenReturn(true);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            aiRecommender.getMultipleRecommendations(mockPreferences, 0);
        });
        assertEquals("Count must be between 1 and 10", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção se o count for maior que 10")
    void testGetMultipleRecommendations_CountTooHigh() {
        when(mockPreferences.isComplete()).thenReturn(true);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            aiRecommender.getMultipleRecommendations(mockPreferences, 11);
        });
        assertEquals("Count must be between 1 and 10", exception.getMessage());
    }

    //teste de sucesso
    @Test
    @DisplayName("Deve processar resposta JSON válida corretamente")
    void testGetMultipleRecommendations_Success() {
        setupMockPreferences(); // Helper para configurar mocks

        // JSON simulado que a AI retornaria
        String jsonResponse = """
            {
                "destinations": [
                    {
                        "city_name": "Tokyo, Japan",
                        "budget": 150,
                        "tags": ["culture", "food", "tech"],
                        "reason": "Perfect mix of tradition and future."
                    },
                    {
                        "city_name": "Lisbon, Portugal",
                        "budget": 100,
                        "tags": ["sunny", "history", "friendly"],
                        "reason": "Great weather and history."
                    }
                ]
            }
            """;

        // configurar os mocks
        when(mockResponse.text()).thenReturn(jsonResponse);//devolve o JSON criado

        // mock do metodo generateContent()
        when(mockModels.generateContent(
                (String) any(String.class),
                (String) any(String.class),
                any(GenerateContentConfig.class)
        )).thenReturn(mockResponse);

        // executa
        List<JSONObject> results = aiRecommender.getMultipleRecommendations(mockPreferences, 2);

        // verifica
        assertNotNull(results);
        assertEquals(2, results.size(), "Deve retornar 2 destinos");

        JSONObject tokyo = results.get(0);
        assertEquals("Tokyo, Japan", tokyo.getString("city_name"));
        assertEquals(150, tokyo.getInt("budget"));
        assertEquals("Perfect mix of tradition and future.", tokyo.getString("reason"));

        assertEquals(3, tokyo.getJSONArray("tags").length());
        assertEquals("culture", tokyo.getJSONArray("tags").getString(0));
    }

    @Test
    @DisplayName("Deve lidar corretamente com listas de exclusão e tags personalizadas vazias ou nulas")
    void testGetRecommendation_WithNullLists() {
        setupMockPreferences();

        String jsonResponse = "{\"destinations\": [{\"city_name\": \"Paris, France\", \"budget\": 200, \"tags\": [], \"reason\": \"Romance\"}]}";
        when(mockResponse.text()).thenReturn(jsonResponse);

        when(mockModels.generateContent(
                (String) any(String.class),
                (String) any(String.class),
                any(GenerateContentConfig.class)
        )).thenReturn(mockResponse);

        // passar null nas listas opcionais
        JSONObject result = aiRecommender.getRecommendation(mockPreferences, null, null);

        assertNotNull(result);
        assertEquals("Paris, France", result.getString("city_name"));
    }

    //testes de erro, quando a IA da erro

    @Test
    @DisplayName("Deve envolver exceções da API numa RuntimeException")
    void testApiFailureHandling() {
        setupMockPreferences();

        // Simular erro na chamada da API
        // Chamada direta ao mockModels
        when(mockModels.generateContent(
                (String) any(String.class),
                (String) any(String.class),
                any(GenerateContentConfig.class)
        )).thenThrow(new RuntimeException("API Quota Exceeded"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            aiRecommender.getMultipleRecommendations(mockPreferences, 1);
        });

        assertTrue(exception.getMessage().contains("Failed to get AI recommendations"));
    }

    @Test
    @DisplayName("Deve lançar erro se a AI retornar lista vazia")
    void testEmptyResponseHandling() {
        setupMockPreferences();

        // JSON valido mas com array vazio
        String emptyJson = "{\"destinations\": []}";

        when(mockResponse.text()).thenReturn(emptyJson);
        when(mockModels.generateContent(
                (String) any(String.class),
                (String) any(String.class),
                any(GenerateContentConfig.class)
        )).thenReturn(mockResponse);

        // getRecommendation (singular) espera pelo menos 1 resultado
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            aiRecommender.getRecommendation(mockPreferences);
        });

        assertEquals("AI returned no recommendations", exception.getMessage());
    }

    // --- testInvalidPreferences ---
    @Test
    @DisplayName("Deve falhar se as preferências estiverem incompletas")
    void testInvalidPreferences() {
        when(mockPreferences.isComplete()).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () ->
                aiRecommender.getMultipleRecommendations(mockPreferences, 3)
        );
    }

    // --- testPromptGeneration & testResponseParsing ---
    @Test
    @DisplayName("Deve gerar o prompt com os dados corretos e fazer parse da resposta")
    void testPromptAndParsing() {
        // 1. Preparar Dados
        setupMockPreferences();
        String fakeJson = "{\"destinations\": [{\"city_name\": \"Tokyo, Japan\", \"budget\": 150, \"tags\": [], \"reason\": \"Test\"}]}";
        when(mockResponse.text()).thenReturn(fakeJson);
        when(mockModels.generateContent(any(String.class), any(String.class), any(GenerateContentConfig.class)))
                .thenReturn(mockResponse);

        // 2. Executar
        List<JSONObject> results = aiRecommender.getMultipleRecommendations(mockPreferences, 1);

        // 3. Verificar Parsing (testResponseParsing)
        assertEquals(1, results.size());
        assertEquals("Tokyo, Japan", results.get(0).getString("city_name"));

        // 4. Verificar Prompt (testPromptGeneration) - Capturamos o que foi enviado para a AI
        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockModels).generateContent(any(String.class), promptCaptor.capture(), any(GenerateContentConfig.class));

        String promptGerada = promptCaptor.getValue();
        // Verifica se o prompt contém palavras-chave das preferências
        assertTrue(promptGerada.contains("Mid-Range")); // Do Budget
        assertTrue(promptGerada.contains("A Bit of Both"));  // Do Pace
    }

    @Test
    @DisplayName("Deve lidar com JSON malformado ou sem o campo 'destinations'")
    void testMalformedJsonResponse() {
        setupMockPreferences();

        // Simula um JSON que a API pode devolver em caso de erro ou alucinação
        String badJson = "{ \"error\": \"I cannot answer that\" }";

        when(mockResponse.text()).thenReturn(badJson);

        // Usar anyString() para evitar ambiguidade
        when(mockModels.generateContent(
                anyString(),
                anyString(),
                any(GenerateContentConfig.class)
        )).thenReturn(mockResponse);

        // O AIRecommender envolve erros de parsing JSON numa RuntimeException
        assertThrows(RuntimeException.class, () -> {
            aiRecommender.getMultipleRecommendations(mockPreferences, 1);
        }, "Deve lançar exceção se o JSON não tiver o array 'destinations'");
    }

    //helper
    private void setupMockPreferences() {
        when(mockPreferences.isComplete()).thenReturn(true);
        // configura valores falsos para os Getters usados na Prompt
        when(mockPreferences.getVibe()).thenReturn(TravelPreferences.Vibe.CITY_CULTURE);
        when(mockPreferences.getBudgetLevel()).thenReturn(TravelPreferences.BudgetLevel.MID_RANGE);
        when(mockPreferences.getPace()).thenReturn(TravelPreferences.TravelPace.BALANCED);
        when(mockPreferences.getCompany()).thenReturn(TravelPreferences.TravelCompany.PARTNER);
        when(mockPreferences.getClimate()).thenReturn(TravelPreferences.ClimatePreference.WARM_MILD);
    }
}