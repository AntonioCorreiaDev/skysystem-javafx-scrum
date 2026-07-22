package pt.isec.skysystem.model;

import com.amadeus.exceptions.ResponseException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.isec.skysystem.model.data.*;
import pt.isec.skysystem.model.auth.AuthService;
import pt.isec.skysystem.model.auth.DatabaseAuthService;
import pt.isec.skysystem.model.data.TripNotification;
import pt.isec.skysystem.model.data.dao.SavedTripsDAO;
import pt.isec.skysystem.model.data.dao.SuggestionDAO;
import pt.isec.skysystem.model.data.dao.TravelPreferencesDAO;
import pt.isec.skysystem.model.data.dao.UserDAO;
import pt.isec.skysystem.model.service.FlightMonitorService;
import pt.isec.skysystem.model.service.AmadeusService;
import pt.isec.skysystem.model.data.flights.Trip;
import pt.isec.skysystem.viewmodel.OnboardingQuizViewModel;

import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Facade for the Data/Model Layer
 * Provides a simplified interface to interact with user preferences and AI
 * recommendations
 */
public class DataFacade {

    private static DataFacade instance;
    private static final Logger logger = LoggerFactory.getLogger(DataFacade.class);
    private final PreferencesManager preferencesManager;
    private final AIRecommender aiRecommender;
    private final AuthService authService;
    private FlightMonitorService fms;

    private List<Suggestion> lastAiSuggestions = new ArrayList<>();

    private final AmadeusService amadeusService;
    private List<String> customSelectedTags = new ArrayList<>();
    private final Map<String, String> imageCache = new HashMap<>();
    private final Map<String, String> cityNameCache = new HashMap<>();

    private String lastPage = "";
    private String flightToSearch = "";
    UserDAO userDAO;
    SavedTripsDAO savedTripsDAO;
    TravelPreferencesDAO travelPreferencesDAO;
    SuggestionDAO suggestionDAO;

    private DataFacade() {
        this.preferencesManager = PreferencesManager.getInstance();
        this.aiRecommender = new AIRecommender();
        this.authService = new DatabaseAuthService();
        this.amadeusService = new AmadeusService();
        DataBaseManager.initializeDatabase();
        DataBaseManager.runMigrationIfNeeded();
        this.userDAO = new UserDAO();
        this.savedTripsDAO = new SavedTripsDAO();
        this.travelPreferencesDAO = new TravelPreferencesDAO();
        this.suggestionDAO = new SuggestionDAO();
        this.fms = new FlightMonitorService(this, amadeusService);
    }

    public static DataFacade getInstance() {
        if (instance == null) {
            instance = new DataFacade();
        }
        return instance;
    }

    // ==================== FLIGHT SEARCH ====================

    public void setLastPage(String name) {
        this.lastPage = name;
    }

    public void setFlightToSearch(String cityName) {
        this.flightToSearch = cityName;
    }

    public String getLastPage() {
        return lastPage;
    }

    public String getFlightToSearch() {
        return flightToSearch;
    }

    public List<Trip> searchFlights(String origin, String dest, String depDate, String retDate, int nPassengers,
            boolean isDirect) {
        try {
            AmadeusService.SearchResult result = amadeusService.searchFlights(origin, dest, depDate, retDate,
                    nPassengers, isDirect);

            if (result.getLocations() != null && !result.getLocations().isEmpty()) {
                updateCityCache(result.getLocations());
            }

            return result.getTrips();
        } catch (ResponseException e) {
            throw new RuntimeException("Error searching flights via Amadeus: " + e.getMessage(), e);
        }
    }

    public String searchLocation(String keyword) {
        try {
            return amadeusService.searchLocations(keyword);
        } catch (ResponseException e) {
            System.err.println("Error searching location: " + e.getMessage());
            return null;
        }
    }

    public String getCityName(String iataCode) {
        if (iataCode == null || iataCode.isEmpty())
            return "";

        if (cityNameCache.containsKey(iataCode)) {
            return cityNameCache.get(iataCode);
        }

        String cityName = amadeusService.getLocationName(iataCode);

        cityNameCache.put(iataCode, cityName);

        return cityName;
    }

    public void updateCityCache(Map<String, String> newLocations) {
        if (newLocations != null && !newLocations.isEmpty()) {
            cityNameCache.putAll(newLocations);
        }
    }

    // ==================== AUTHENTICATION ====================

    public boolean register(String username, String password, String email) {
        return authService.register(username, password, email);
    }

    public boolean login(String email, String password) {
        String username = authService.login(email, password);
        if (username != null) {
            setCurrentUser(username, email);
            loadSavedFlightsFromDatabase();
            loadSavedSuggetionsFromDatabase();
            fms.setDaemon(true);
            fms.run();

            return true;
        }
        return false;
    }

    public void logout() {
        preferencesManager.setCurrentUser(null);
        clearLastAiSuggestions();
        clearCustomTags();
        logger.info("User logged out successfully");
    }

    // ==================== USER MANAGEMENT ====================
    /**
     * Sets the active user for the current session
     */
    public void setCurrentUser(String username, String email) {
        try {
            if (authService.userExists(email)) {
                boolean onBoardingCOmpleted = authService.getOnboardingStatus();
                UserProfile user = new UserProfile(username, email, onBoardingCOmpleted);
                long userId = userDAO.getId(email);
                if (userId != -1) {
                    user.setOnboardingCompleted(userDAO.isOnboardingCompleted(userId));
                    user.setPreferences(travelPreferencesDAO.loadPreferencesByUserId(userId));
                }
                preferencesManager.setCurrentUser(user);
            }
        } catch (Exception e) {
            logger.error("Error setting current user", e);
        }

    }

    /**
     * Gets the currently active user
     */
    public UserProfile getCurrentUser() {
        return preferencesManager.getCurrentUser();
    }

    /**
     * Checks if there is an active user
     */
    public boolean hasCurrentUser() {
        return preferencesManager.getCurrentUser() != null;
    }

    // ==================== PREFERENCES MANAGEMENT ====================

    /**
     * Saves travel preferences for the current user
     * Returns true if saved successfully
     */
    public boolean saveUserPreferences(TravelPreferences preferences) {
        if (!hasCurrentUser()) {
            return false;
        }

        try {
            travelPreferencesDAO.savePreferences(authService.getCurrentUserId(), preferences);
        } catch (Exception e) {
            logger.error("Error saving user preferences to database", e);
            return false;
        }

        return preferencesManager.savePreferences(preferences);
    }

    /**
     * Gets the travel preferences of the current user
     */
    public TravelPreferences getCurrentUserPreferences() {
        UserProfile user = getCurrentUser();
        if (user == null) {
            return null;
        }

        return user.getPreferences();
    }

    /**
     * Checks if the current user needs to complete the onboarding quiz
     */
    public boolean currentUserNeedsOnboarding() {
        return preferencesManager.currentUserNeedsOnboarding();
    }

    /**
     * Resets the current user's preferences (useful for retaking the quiz)
     */
    public void resetCurrentUserPreferences() {
        preferencesManager.resetCurrentUserPreferences();
    }

    /**
     * Completes the onboarding process by creating and saving preferences
     * Accepts raw enum values from ViewModel
     */
    public boolean completeOnboarding(
            String vibeName,
            String budgetLevelName,
            String paceName,
            String companyName,
            String climateName) {

        if (!hasCurrentUser()) {
            return false;
        }

        try {
            TravelPreferences preferences = new TravelPreferences();

            // Convert vibe name to enum
            try {
                TravelPreferences.Vibe vibe = TravelPreferences.Vibe.valueOf(vibeName);
                preferences.setVibe(vibe);
            } catch (IllegalArgumentException e) {
                logger.error("Invalid Vibe name: {}", vibeName, e);
                return false;
            }

            // Convert other enum names
            try {
                preferences.setBudgetLevel(TravelPreferences.BudgetLevel.valueOf(budgetLevelName));
                preferences.setPace(TravelPreferences.TravelPace.valueOf(paceName));
                preferences.setCompany(TravelPreferences.TravelCompany.valueOf(companyName));
                preferences.setClimate(TravelPreferences.ClimatePreference.valueOf(climateName));
            } catch (IllegalArgumentException e) {
                logger.error("Invalid enum name for preference", e);
                return false;
            }

            if (!preferences.isComplete()) {
                return false;
            }

            return saveUserPreferences(preferences);
        } catch (Exception e) {
            logger.error("Error completing onboarding", e);
            return false;
        }
    }

    /**
     * Updates the onboarding status for the current user.
     */
    public boolean updateOnboardingStatus(boolean completed) {
        if (!hasCurrentUser()) {
            return false;
        }
        boolean success = authService.updateOnboardingStatus(completed);
        if (success) {
            getCurrentUser().setOnboardingCompleted(completed);
        }
        return success;
    }

    // ==================== AI RECOMMENDATION / SUGGESTIONS ====================

    /**
     * Devolve uma lista de objetos Suggestion (normalmente 3), aplicando as tags
     * personalizadas
     * se existirem, e excluindo destinos fornecidos.
     *
     * @param excludeDestinations Lista de nomes de destinos a excluir.
     * @return Uma lista de objetos Suggestion.
     */
    public List<Suggestion> getAiSuggestions(List<String> excludeDestinations) {
        return getAiRecommendations(3, excludeDestinations, this.customSelectedTags);
    }

    /**
     * Obtém uma única nova sugestão, aplicando as tags personalizadas se existirem,
     * e excluindo destinos fornecidos.
     *
     * @param excludeDestinations Lista de nomes de destinos a excluir.
     * @return Uma única Suggestion ou null.
     */
    public Suggestion getSingleNewSuggestion(List<String> excludeDestinations) {
        List<Suggestion> newSuggestions = getAiRecommendations(1, excludeDestinations, this.customSelectedTags);

        if (newSuggestions.isEmpty()) {
            return null;
        }
        return newSuggestions.get(0);
    }

    /**
     * Método centralizado e privado para buscar recomendações da AI.
     */
    private List<Suggestion> getAiRecommendations(int count, List<String> excludeDestinations,
            List<String> customTags) {
        UserProfile user = getCurrentUser();
        if (user == null) {
            throw new IllegalStateException("No current user - cannot get suggestions");
        }
        if (user.needsOnboarding()) {
            throw new IllegalStateException("User must complete onboarding quiz first");
        }

        TravelPreferences preferences = user.getPreferences();

        List<String> finalExcludeDestinations = excludeDestinations != null ? excludeDestinations
                : Collections.emptyList();
        List<String> finalCustomTags = customTags != null ? customTags : Collections.emptyList();

        try {
            List<JSONObject> recommendationList = aiRecommender.getMultipleRecommendations(
                    preferences,
                    count,
                    finalExcludeDestinations,
                    finalCustomTags);

            List<Suggestion> suggestions = recommendationList.parallelStream()
                    .map(this::parseJsonToSuggestion)
                    .collect(Collectors.toList());

            if (count > 1) {
                this.lastAiSuggestions = suggestions;
            }
            logger.info("Generated {} suggestions ({} count) with {} custom tags",
                    suggestions.size(),
                    count,
                    finalCustomTags.size());
            return suggestions;

        } catch (Exception e) {
            logger.error("Failed to get AI recommendations (count: {})", count, e);
            return Collections.emptyList();
        }
    }

    /**
     * Gets an AI-generated travel recommendation for the current user
     * Throws IllegalStateException if user hasn't completed onboarding
     */
    public JSONObject getRecommendationForCurrentUser() {
        UserProfile user = getCurrentUser();
        if (user == null) {
            throw new IllegalStateException("No current user - cannot get recommendations");
        }

        if (user.needsOnboarding()) {
            throw new IllegalStateException("User must complete onboarding quiz first");
        }

        TravelPreferences preferences = user.getPreferences();
        return aiRecommender.getRecommendation(preferences);
    }

    /**
     * Gets an AI recommendation using custom preferences (for testing or "what if"
     * scenarios)
     */
    public JSONObject getRecommendation(TravelPreferences preferences) {
        return aiRecommender.getRecommendation(preferences);
    }

    // ==================== SUGGESTION STATE MANAGEMENT ====================

    /**
     * Devolve a última lista de sugestões geradas sem as recalcular
     */
    public List<Suggestion> getLastAiSuggestions() {
        return this.lastAiSuggestions;
    }

    /**
     * Limpa as sugestões guardadas
     */
    public void clearLastAiSuggestions() {
        this.lastAiSuggestions.clear();
    }

    /**
     * Replaces a specific suggestion in the stored list with a new one.
     * This ensures persistence when navigating between pages.
     */
    public void replaceStoredSuggestion(Suggestion oldSuggestion, Suggestion newSuggestion) {
        int index = lastAiSuggestions.indexOf(oldSuggestion);
        if (index != -1) {
            lastAiSuggestions.set(index, newSuggestion);
            logger.info("Updated stored suggestion at index {}", index);
        } else {
            logger.warn("Could not find suggestion to replace in stored list");
        }
    }

    // ==================== TAG MANAGEMENT ====================
    public void setCustomTags(List<String> tags) {
        if (tags == null) {
            this.customSelectedTags.clear();
        } else {
            this.customSelectedTags = new ArrayList<>(tags);
        }
        logger.info("Custom tags updated: {} tags", this.customSelectedTags.size());
    }

    /**
     * Obtém as tags personalizadas selecionadas
     */
    public List<String> getCustomTags() {
        return new ArrayList<>(customSelectedTags);
    }

    /**
     * Verifica se há tags personalizadas definidas
     */
    public boolean hasCustomTags() {
        return !customSelectedTags.isEmpty();
    }

    /**
     * Limpa as tags personalizadas
     */
    public void clearCustomTags() {
        customSelectedTags.clear();
        logger.info("Custom tags cleared");
    }

    // ==================== UTILITY & MAPPERS ====================

    /**
     * converter JSONObject para Suggestion
     */
    private Suggestion parseJsonToSuggestion(JSONObject recommendationJson) {
        String cityName = recommendationJson.optString("city_name", "N/A");
        String reason = recommendationJson.optString("reason", "N/A");
        String budget = recommendationJson.optInt("budget", 0) + " €/day";

        String imageUrl = getValidImageUrl(cityName);

        System.out.println("DataFacade: Generated image URL: " + imageUrl);

        JSONArray tagsArray = recommendationJson.optJSONArray("tags");
        String tags = tagsArray != null ? tagsArray.toList().stream()
                .map(Object::toString)
                .collect(Collectors.joining(", ")) : "";

        return new Suggestion(cityName, tags, reason, budget, imageUrl);
    }

    /**
     * Gets a valid image URL for a search term with custom dimensions
     *
     * @param searchTerm The search term
     * @param width      Desired image width
     * @param height     Desired image height
     * @return A valid image URL or fallback placeholder
     */
    public String getValidImageUrl(String searchTerm, int width, int height) {
        String cacheKey = searchTerm + "_" + width + "x" + height;

        // Check cache first
        if (imageCache.containsKey(cacheKey)) {
            return imageCache.get(cacheKey);
        }

        try {
            String encodedTerm = URLEncoder.encode(searchTerm, java.nio.charset.StandardCharsets.UTF_8.toString());
            String dimensions = width + "/" + height;

            String primaryUrl = "https://loremflickr.com/" + dimensions + "/" + encodedTerm + ",landmark";
            String finalPrimary = getFinalRedirectedUrl(primaryUrl);
            if (finalPrimary != null) {
                imageCache.put(cacheKey, finalPrimary);
                return finalPrimary;
            }

            String fallbackUrl = "https://loremflickr.com/" + dimensions + "/" + encodedTerm + ",travel";
            String finalFallback = getFinalRedirectedUrl(fallbackUrl);
            if (finalFallback != null) {
                imageCache.put(cacheKey, finalFallback);
                return finalFallback;
            }

            String finalAttemptUrl = "https://loremflickr.com/" + dimensions + "/" + encodedTerm;
            String finalAttempt = getFinalRedirectedUrl(finalAttemptUrl);
            if (finalAttempt != null) {
                imageCache.put(cacheKey, finalAttempt);
                return finalAttempt;
            }

            String placeholder = "https://placehold.co/" + width + "x" + height + "?text=" + encodedTerm;
            imageCache.put(cacheKey, placeholder);
            return placeholder;
        } catch (Exception e) {
            logger.error("Error generating image URL", e);
            return "https://placehold.co/" + width + "x" + height + "?text=" + searchTerm;
        }
    }

    public String getValidImageUrl(String cityName) {
        // Check cache first
        if (imageCache.containsKey(cityName)) {
            return imageCache.get(cityName);
        }

        try {
            String encodedCity = URLEncoder.encode(cityName, java.nio.charset.StandardCharsets.UTF_8.toString());

            String primaryUrl = "https://loremflickr.com/500/500/" + encodedCity + ",landmark";
            String finalPrimary = getFinalRedirectedUrl(primaryUrl);
            if (finalPrimary != null) {
                imageCache.put(cityName, finalPrimary);
                return finalPrimary;
            }

            String fallbackUrl = "https://loremflickr.com/500/500/" + encodedCity + ",travel";
            String finalFallback = getFinalRedirectedUrl(fallbackUrl);
            if (finalFallback != null) {
                imageCache.put(cityName, finalFallback);
                return finalFallback;
            }

            String finalAttemptUrl = "https://loremflickr.com/500/500/" + encodedCity;
            String finalAttempt = getFinalRedirectedUrl(finalAttemptUrl);
            if (finalAttempt != null) {
                imageCache.put(cityName, finalAttempt);
                return finalAttempt;
            }

            String placeholder = "https://placehold.co/500x500?text=" + encodedCity;
            imageCache.put(cityName, placeholder);
            return placeholder;
        } catch (Exception e) {
            logger.error("Error generating image URL", e);
            return "https://placehold.co/800x500?text=" + cityName;
        }
    }

    /**
     * Obtém o histórico de notificações/alterações para uma viagem específica.
     * @param trip A viagem da qual queremos ver o histórico.
     * @return Lista de notificações ou lista vazia se não houver user logado.
     */
    public List<TripNotification> getTripHistory(Trip trip) {
        if (!hasCurrentUser()) {
            return new ArrayList<>();
        }
        // Passa o ID do utilizador logado e o objeto Trip para o DAO
        return savedTripsDAO.getNotificationsForTrip(authService.getCurrentUserId(), trip);
    }

    public void addTripNotification(Trip trip, String message) {
        // 1. Verificação de segurança: Só guarda se houver alguém logado
        if (!hasCurrentUser()) {
            logger.warn("Tentativa de guardar notificação sem utilizador logado. Ignorado.");
            return;
        }

        // 2. Obter o ID do utilizador atual através do serviço de autenticação
        long userId = authService.getCurrentUserId();

        // 3. Chamar o DAO para guardar (o DAO vai descobrir o ID da viagem internamente)
        savedTripsDAO.addNotification(userId, trip, message);

        logger.info("Notificação guardada para a viagem {} do user {}", trip.getId(), userId);
    }

    private String getFinalRedirectedUrl(String urlString) {
        try {
            URL url = new URI(urlString).toURL();
            HttpURLConnection connection = null;

            for (int i = 0; i < 5; i++) {
                connection = (HttpURLConnection) url.openConnection();
                connection.setInstanceFollowRedirects(false);
                connection.setRequestMethod("HEAD");
                connection.setConnectTimeout(1000);
                connection.connect();

                int responseCode = connection.getResponseCode();

                if (responseCode >= 300 && responseCode < 400) {
                    String location = connection.getHeaderField("Location");
                    if (location == null)
                        return null;

                    url = url.toURI().resolve(location).toURL();
                } else {
                    String finalUrl = url.toString();

                    // Valid if it's a 200 OK and NOT the default placeholder image
                    if (!finalUrl.contains("defaultImage") && responseCode == 200) {
                        return finalUrl;
                    }
                    return null;
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Gets a human-readable summary of the current user's preferences
     */
    public String getCurrentUserPreferencesSummary() {
        TravelPreferences prefs = getCurrentUserPreferences();
        if (prefs == null) {
            return "No preferences set";
        }
        return AIRecommender.getPreferencesSummary(prefs);
    }

    // ==================== QUIZ OPTIONS SETTERS ====================
    public List<OnboardingQuizViewModel.OptionData> getVibeOptions() {
        List<OnboardingQuizViewModel.OptionData> options = new ArrayList<>();
        for (TravelPreferences.Vibe vibe : TravelPreferences.Vibe.values()) {
            options.add(new OnboardingQuizViewModel.OptionData(vibe.name(), vibe.getDisplayName()));
        }
        return options;
    }

    public List<OnboardingQuizViewModel.OptionData> getBudgetOptions() {
        List<OnboardingQuizViewModel.OptionData> options = new ArrayList<>();
        for (TravelPreferences.BudgetLevel budget : TravelPreferences.BudgetLevel.values()) {
            options.add(new OnboardingQuizViewModel.OptionData(budget.name(), budget.getDisplayName()));
        }
        return options;
    }

    public List<OnboardingQuizViewModel.OptionData> getPaceOptions() {
        List<OnboardingQuizViewModel.OptionData> options = new ArrayList<>();
        for (TravelPreferences.TravelPace pace : TravelPreferences.TravelPace.values()) {
            options.add(new OnboardingQuizViewModel.OptionData(pace.name(), pace.getDisplayName()));
        }
        return options;
    }

    public List<OnboardingQuizViewModel.OptionData> getCompanyOptions() {
        List<OnboardingQuizViewModel.OptionData> options = new ArrayList<>();
        for (TravelPreferences.TravelCompany company : TravelPreferences.TravelCompany.values()) {
            options.add(new OnboardingQuizViewModel.OptionData(company.name(), company.getDisplayName()));
        }
        return options;
    }

    public List<OnboardingQuizViewModel.OptionData> getClimateOptions() {
        List<OnboardingQuizViewModel.OptionData> options = new ArrayList<>();
        for (TravelPreferences.ClimatePreference climate : TravelPreferences.ClimatePreference.values()) {
            options.add(new OnboardingQuizViewModel.OptionData(climate.name(), climate.getDisplayName()));
        }
        return options;
    }

    // REVER

    public boolean removeFavoriteDestination(String destination, String imgUrl) {
        UserProfile user = getCurrentUser();
        if (user != null) {
            if (user.removeFavoriteDestination(destination, imgUrl)) {
                suggestionDAO.removeFavoriteSuggestion(destination, authService.getCurrentUserId());
                logger.info("Removed favorite destination from database: {}", destination);
                return true;
            }
        }
        return false;
    }


    public void launchMonitor() {
        FlightMonitorService monitorService = new FlightMonitorService(this, amadeusService);
        monitorService.startMonitoring();
    }

    public void loadSavedSuggetionsFromDatabase() {
        UserProfile user = getCurrentUser();

        if (user != null) {
            long userId = authService.getCurrentUserId();

            if (userId > 0) {
                try {

                    List<Suggestion> loadedSuggetions = suggestionDAO.getFavoritesSuggestions(userId);

                    user.setSavedSuggetions(loadedSuggetions);

                    logger.info("Loaded {} favorite flights from database for user ID {}", loadedSuggetions.size(),
                            userId);
                } catch (Exception e) {
                    logger.error("Failed to load saved flights from database for user ID " + userId, e);
                }
            } else {
                logger.warn("Attempted to load saved flights, but current user has an invalid ID ({})", userId);
            }
        } else {
            logger.warn("Attempted to load saved flights, but no user is currently logged in.");
        }
    }

    public void addFavouriteDestination(String destination, String imgUrl, String tags, String reason,
            String priceRange) {
        UserProfile user = getCurrentUser();

        if (user != null) {
            user.addFavoriteDestination(destination, imgUrl, tags, reason, priceRange);
            suggestionDAO.save(destination, imgUrl, tags, reason, priceRange, authService.getCurrentUserId());
        }
    }

    public LinkedList<Suggestion> getFavoriteDestinations() {
        UserProfile user = getCurrentUser();

        if (user != null && user.getFavoriteDestinations() != null) {
            return new LinkedList<>(user.getFavoriteDestinations());
        }

        // Return empty list if user is null OR the list hasn't been created yet
        return new LinkedList<>();
    }
    // ==================== SAVED FLIGHTS MANAGEMENT ====================

    public void saveFlight(Trip trip) {
        UserProfile user = getCurrentUser();

        if (user != null) {
            long userId = authService.getCurrentUserId();
            boolean sucess = savedTripsDAO.saveTrip(userId, trip);
            user.addSavedFlight(trip);

            if (sucess) {
                logger.info("Flight saved successfully: " + trip.getId());
            } else {
                logger.error("Error saving flight: " + trip.getId());
            }
        }
    }

    public void removeSavedFlight(Trip trip) {
        UserProfile user = getCurrentUser();
        if (user != null) {
            user.removeSavedFlight(trip);
            String returnFlightNumber = trip.getReturnFlight() != null
                    ? trip.getReturnFlight().getFlightNumber()
                    : null;
            savedTripsDAO.removeTrip(authService.getCurrentUserId(),
                    trip.getOutboundFlight().getFlightNumber(),
                    returnFlightNumber);
            logger.info("Flight removed: " + trip.getId());

        }
    }

    public List<Trip> getSavedFlights() {
        UserProfile user = getCurrentUser();
        if (user != null) {
            return user.getSavedFlights();
        }
        return new ArrayList<>();
    }

    public boolean isFlightSaved(String outboundFlightNumber, String returnFlightNumber) {
        UserProfile user = getCurrentUser();
        return user != null && user.isFlightSaved(outboundFlightNumber, returnFlightNumber);
    }

    public void loadSavedFlightsFromDatabase() {
        UserProfile user = getCurrentUser();

        if (user != null) {
            long userId = authService.getCurrentUserId();

            if (userId > 0) {
                try {
                    List<Trip> loadedFlights = savedTripsDAO.getSavedTrips(userId);

                    user.setSavedFlights(loadedFlights);

                    logger.info("Loaded {} favorite flights from database for user ID {}", loadedFlights.size(),
                            userId);
                } catch (Exception e) {
                    logger.error("Failed to load saved flights from database for user ID " + userId, e);
                }
            } else {
                logger.warn("Attempted to load saved flights, but current user has an invalid ID ({})", userId);
            }
        } else {
            logger.warn("Attempted to load saved flights, but no user is currently logged in.");
        }
    }

    // ==================== FAVORITES FLIGHTS MANAGEMENT ====================

    public boolean isFavorite(String outboundFlightNumber, String returnFlightNumber) {
        if (!hasCurrentUser()) {
            return false;
        }
        return savedTripsDAO.isTripSaved(authService.getCurrentUserId(), outboundFlightNumber, returnFlightNumber);
    }

    public String getNomeUtilizadorLogado() {
        long userId = authService.getCurrentUserId();

        if (userId != -1) {
            String nome = userDAO.getNomeUser(userId);
            if (nome != null && !nome.isEmpty()) {
                return nome;
            }
        }

        UserProfile user = getCurrentUser();
        if (user != null) {
            return user.getUsername();
        }

        return "PILOT";
    }

    /**
     * Verifica se um destino específico já está na lista de favoritos do
     * utilizador.
     * Útil para definir o estado inicial do botão de coração.
     * 
     * @param destinationName O nome da cidade/destino.
     * @return true se for favorito, false caso contrário.
     */
    public boolean isFavoriteDestination(String destinationName) {
        if (destinationName == null)
            return false;

        // 1. Obtém a lista de favoritos atual (usa o metodo que já tem)
        List<Suggestion> favs = getFavoriteDestinations();

        if (favs == null || favs.isEmpty()) {
            return false;
        }

        // 2. Percorre a lista à procura do nome
        for (Suggestion s : favs) {
            // equalsIgnoreCase para garantir que "Paris" é igual a "paris"
            if (s.getDestination().equalsIgnoreCase(destinationName)) {
                return true;
            }
        }

        return false;
    }
}