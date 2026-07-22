package pt.isec.skysystem.model.data;

import com.google.common.collect.ImmutableMap;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Schema;
import com.google.genai.types.Type;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AIRecommender {
    private static final Logger logger = LoggerFactory.getLogger(AIRecommender.class);

    private static final String API_KEY = "AIzaSyAN8vduJxj_ckdQKWn2iIix72DRNA6OKP8";
    private static final String MODEL = "gemini-flash-latest";

    private final Client client;

    public AIRecommender() {
        this.client = Client.builder().apiKey(API_KEY).build();
        logger.info("AIRecommender initialized");
    }

    // --- Public Recommendation Methods ---

    /**
     * Recomenda um destino.
     */
    public JSONObject getRecommendation(TravelPreferences preferences) {
        return getRecommendation(preferences, Collections.emptyList(), Collections.emptyList());
    }

    /**
     * Recomenda um destino, excluindo os que já foram fornecidos.
     * 
     * @param preferences         Preferências do utilizador
     * @param excludeDestinations Lista de nomes de destinos (ex: "Paris, France") a
     *                            excluir
     * @param customTags          Lista de tags personalizadas para priorizar (pode
     *                            ser null/vazia)
     */
    public JSONObject getRecommendation(
        TravelPreferences preferences,
        List<String> excludeDestinations,
        List<String> customTags) {
        // Reuse the logic from getMultipleRecommendations
        List<JSONObject> results = getMultipleRecommendations(preferences, 1, excludeDestinations, customTags);
        if (results.isEmpty()) {
            throw new RuntimeException("AI returned no recommendations");
        }
        return results.get(0);
    }

    /**
     * Recomenda um destino, excluindo os que já foram fornecidos (Legacy
     * signature).
     */
    public JSONObject getRecommendation(TravelPreferences preferences, List<String> excludeDestinations) {
        return getRecommendation(preferences, excludeDestinations, Collections.emptyList());
    }

    /**
     * Recomenda múltiplos destinos sem exclusões ou tags personalizadas.
     */
    public List<JSONObject> getMultipleRecommendations(TravelPreferences preferences, int count) {
        return getMultipleRecommendations(preferences, count, Collections.emptyList(), Collections.emptyList());
    }

    /**
     * Recomenda múltiplos destinos, excluindo os que já foram fornecidos e
     * aplicando tags personalizadas.
     * 
     * @param preferences         Preferências do utilizador
     * @param count               O número de recomendações a gerar
     * @param excludeDestinations Lista de nomes de destinos (ex: "Paris, France") a
     *                            excluir
     * @param customTags          Lista de tags personalizadas para priorizar (pode
     *                            ser null/vazia)
     */
    public List<JSONObject> getMultipleRecommendations(
            TravelPreferences preferences,
            int count,
            List<String> excludeDestinations,
            List<String> customTags) {

        if (preferences == null) {
            throw new IllegalArgumentException("TravelPreferences cannot be null");
        }

        if (!preferences.isComplete()) {
            throw new IllegalArgumentException("TravelPreferences must be complete");
        }

        if (count < 1 || count > 10) {
            throw new IllegalArgumentException("Count must be between 1 and 10");
        }

        // Normalize lists to non-null, empty lists if null is passed
        List<String> finalExcludeDestinations = excludeDestinations != null ? excludeDestinations
                : Collections.emptyList();
        List<String> finalCustomTags = customTags != null ? customTags : Collections.emptyList();

        String prompt = buildMultipleDestinationsPrompt(preferences, count, finalExcludeDestinations, finalCustomTags);
        logger.debug("Generated AI prompt for {} destinations: {}", count, prompt);


        GenerateContentConfig config = buildMultipleDestinationsConfig();

        try {
            GenerateContentResponse response = client.models.generateContent(
                    MODEL,
                    prompt,
                    config);

            logger.info("AI recommendations received successfully");
            logger.debug("Response: {}", response.text());

            String jsonStringResponse = response.text();
            JSONObject parsedResponse = new JSONObject(jsonStringResponse);

            //verifica se a chave destinations existe - alterado para resolver erro nos testes
            if (!parsedResponse.has("destinations")) {
                throw new RuntimeException("AI response missing 'destinations' key. Raw response: " + jsonStringResponse);
            }

            JSONArray destinationsArray = parsedResponse.getJSONArray("destinations");

            List<JSONObject> recommendations = new ArrayList<>();
            for (int i = 0; i < destinationsArray.length(); i++) {
                recommendations.add(destinationsArray.getJSONObject(i));
            }

            return recommendations;

        } catch (Exception e) {
            logger.error("Error getting AI recommendations", e);
            // Envolve qualquer erro (API, JSON, Rede) numa RuntimeException com mensagem padronizada
            throw new RuntimeException("Failed to get AI recommendations: " + e.getMessage(), e);
        }
    }

    /**
     * Recomenda múltiplos destinos, excluindo os que já foram fornecidos (Legacy
     * signature).
     */
    public List<JSONObject> getMultipleRecommendations(TravelPreferences preferences, int count,
            List<String> excludeDestinations) {
        return getMultipleRecommendations(preferences, count, excludeDestinations, Collections.emptyList());
    }

    // --- Prompt Builder Methods ---
    private String buildMultipleDestinationsPrompt(TravelPreferences prefs, int count, List<String> excludeDestinations,
            List<String> customTags) {
        StringBuilder prompt = new StringBuilder();

        prompt.append(String.format(
                "You are an expert travel advisor. Recommend %d DIFFERENT travel destinations based on this traveler's profile:\n\n",
                count));
        appendPreferencesSection(prompt, prefs);
        appendCustomTags(prompt, customTags);
        appendMultipleDestinationsInstructions(prompt, count, excludeDestinations);

        return prompt.toString();
    }

    /**
     * Adiciona a secção de tags personalizadas ao prompt se a lista não estiver
     * vazia.
     */
    private void appendCustomTags(StringBuilder prompt, List<String> customTags) {
        if (customTags != null && !customTags.isEmpty()) {
            prompt.append("CUSTOM PREFERENCES (PRIORITY):\n");
            prompt.append("The traveler specifically wants destinations that match these tags:\n");
            for (String tag : customTags) {
                prompt.append(String.format("- %s\n", tag));
            }
            prompt.append("\nPrioritize these tags when making recommendations!\n\n");
        }
    }

    private void appendPreferencesSection(StringBuilder prompt, TravelPreferences prefs) {
        prompt.append("INTERESTS & VIBE:\n");
        TravelPreferences.Vibe vibe = prefs.getVibe();
        prompt.append(String.format("- %s (%s)\n",
                vibe.getDisplayName(),
                String.join(", ", vibe.getTags())));
        prompt.append("\n");

        TravelPreferences.BudgetLevel budget = prefs.getBudgetLevel();
        prompt.append("BUDGET:\n");
        prompt.append(String.format("- Level: %s\n", budget.getDisplayName()));
        prompt.append(String.format("- Daily range: $%d to $%d EUR\n",
                budget.getMinDaily(),
                budget.getMaxDaily()));
        prompt.append(String.format("- Style: %s\n\n", budget.getTag()));

        TravelPreferences.TravelPace pace = prefs.getPace();
        prompt.append("TRAVEL PACE:\n");
        prompt.append(String.format("- Preference: %s\n", pace.getDisplayName()));
        prompt.append(String.format("- Style: %s\n\n", pace.getTag()));

        TravelPreferences.TravelCompany company = prefs.getCompany();
        prompt.append("TRAVEL COMPANY:\n");
        prompt.append(String.format("- Traveling with: %s\n", company.getDisplayName()));
        prompt.append(String.format("- Context tags: %s\n\n",
                String.join(", ", company.getTags())));

        TravelPreferences.ClimatePreference climate = prefs.getClimate();
        prompt.append("PREFERRED CLIMATE:\n");
        prompt.append(String.format("- Preference: %s\n", climate.getDisplayName()));
        prompt.append(String.format("- Climate types: %s\n\n",
                String.join(", ", climate.getTags())));
    }

    private void appendMultipleDestinationsInstructions(StringBuilder prompt, int count,
            List<String> excludeDestinations) {
        prompt.append("TASK:\n");
        prompt.append(String.format(
                "Based on ALL the criteria above, recommend %d DIFFERENT specific destinations that:\n", count));
        prompt.append("1. Match the traveler's interests and vibes\n");
        prompt.append("2. Fit within their budget range\n");
        prompt.append("3. Suit their preferred travel pace\n");
        prompt.append("4. Are appropriate for their travel company type\n");
        prompt.append("5. Have the desired climate\n");
        prompt.append("6. Are geographically diverse (different countries/regions)\n");
        prompt.append("7. Offer different primary experiences\n\n");

        if (!excludeDestinations.isEmpty()) {
            prompt.append("EXCLUSION LIST:\n");
            prompt.append("Do NOT recommend any of the following destinations, as they have already been suggested:\n");
            for (String dest : excludeDestinations) {
                prompt.append(String.format("- %s\n", dest));
            }
            prompt.append("\n");
        }

        prompt.append("OUTPUT FORMAT:\n");
        prompt.append("Provide a JSON response with a 'destinations' array containing objects with:\n");
        prompt.append("- city_name: Full destination name with country (e.g., 'Barcelona, Spain')\n");
        prompt.append(
                "- budget: Realistic daily budget for this destination, excluding flights (integer, within user's range)\n");
        prompt.append(
                "- tags: Array of 3-5 relevant, specific tags describing the destination's vibe (e.g., 'foodie', 'historical', 'nightlife', 'adventure').\n");
        prompt.append(
                "- reason: A 2-4 phrase explanation of why this destination matches their profile (max 350 chars)\n");
    }

    // --- Config and Utility Methods ---

    private GenerateContentConfig buildMultipleDestinationsConfig() {
        Schema cityNameSchema = Schema.builder()
                .type(Type.Known.STRING)
                .description("The name of the recommended city and country (e.g., 'Kyoto, Japan').")
                .build();

        Schema budgetSchema = Schema.builder()
                .type(Type.Known.INTEGER)
                .description("Estimated daily budget in EUR (must be within user's budget range)")
                .build();

        Schema tagItemSchema = Schema.builder()
                .type(Type.Known.STRING)
                .description(
                        "A relevant tag describing the destination's vibe (e.g., 'foodie', 'historical', 'nightlife').")
                .build();

        Schema tagsArraySchema = Schema.builder()
                .type(Type.Known.ARRAY)
                .description("Array of 3-5 relevant tags for this destination.")
                .items(tagItemSchema)
                .build();

        Schema reasonSchema = Schema.builder()
                .type(Type.Known.STRING)
                .description("A brief explanation of why this destination matches the user's profile")
                .build();

        Schema destinationSchema = Schema.builder()
                .type(Type.Known.OBJECT)
                .properties(ImmutableMap.of(
                        "city_name", cityNameSchema,
                        "budget", budgetSchema,
                        "tags", tagsArraySchema,
                        "reason", reasonSchema))
                .build();

        Schema destinationsArraySchema = Schema.builder()
                .type(Type.Known.ARRAY)
                .description("Array of recommended destinations")
                .items(destinationSchema)
                .build();

        Schema responseSchema = Schema.builder()
                .type(Type.Known.OBJECT)
                .properties(ImmutableMap.of(
                        "destinations", destinationsArraySchema))
                .build();

        return GenerateContentConfig.builder()
                .responseMimeType("application/json")
                .candidateCount(1)
                .responseSchema(responseSchema)
                .build();
    }

    public static String getPreferencesSummary(TravelPreferences preferences) {
        if (preferences == null || !preferences.isComplete()) {
            return "Incomplete preferences";
        }

        return String.format(
                "Vibe: %s | Budget: %s | Pace: %s | Company: %s | Climate: %s",
                preferences.getVibe().getDisplayName(),
                preferences.getBudgetLevel().getDisplayName(),
                preferences.getPace().getDisplayName(),
                preferences.getCompany().getDisplayName(),
                preferences.getClimate().getDisplayName());
    }
}