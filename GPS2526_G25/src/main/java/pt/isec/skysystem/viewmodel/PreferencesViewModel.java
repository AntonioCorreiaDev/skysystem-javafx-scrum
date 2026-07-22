package pt.isec.skysystem.viewmodel;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for the Preferences modal
 * Manages travel preferences state
 */
public class PreferencesViewModel {
    private static final Logger logger = LoggerFactory.getLogger(PreferencesViewModel.class);

    private final String pageName="Preferences";

    // Budget properties
    private final DoubleProperty budgetValue;
    private final StringProperty budgetText;

    // Travel Style options
    private final ObservableList<String> travelStyles;
    private final StringProperty selectedTravelStyle;

    // Weather options
    private final ObservableList<String> weatherPreferences;
    private final StringProperty selectedWeather;

    private final ObservableList<String> travelCompanyPreferences;
    private final ObservableList<String> activityLevelPreferences;
    private final StringProperty selectedTravelCompany;
    private final StringProperty selectedActivityLevel;

    // Tags disponíveis e selecionadas
    private final ObservableList<String> availableTags;
    private final ObservableList<String> selectedTags;

    // Constants
    private static final double MIN_BUDGET = 0;
    private static final double MAX_BUDGET = 1000;
    private static final double DEFAULT_BUDGET = 0;

    public PreferencesViewModel() {
        // Initialize budget properties
        this.budgetValue = new SimpleDoubleProperty(DEFAULT_BUDGET);
        this.budgetText = new SimpleStringProperty(String.format("%.0f €", DEFAULT_BUDGET));

        this.availableTags = FXCollections.observableArrayList();
        this.selectedTags = FXCollections.observableArrayList();

        // Initialize travel styles
        this.travelStyles = FXCollections.observableArrayList(
                "Mountain", "City", "Beach", "CountrySide"
        );
        this.selectedTravelStyle = new SimpleStringProperty("");

        // Initialize weather preferences
        this.weatherPreferences = FXCollections.observableArrayList(
                "Hot", "Warm", "No Preferences", "Snowy"
        );
        this.selectedWeather = new SimpleStringProperty("");

        this.travelCompanyPreferences = FXCollections.observableArrayList(
                "Solo", "Friends", "Couple", "Family"
        );
        this.selectedTravelCompany = new SimpleStringProperty("");

        this.activityLevelPreferences = FXCollections.observableArrayList(
                "Relaxed", "Moderate", "High"
        );
        this.selectedActivityLevel = new SimpleStringProperty("");

        setupBidirectionalBinding();
        logger.info("PreferencesViewModel initialized with default budget: {}", DEFAULT_BUDGET);
    }

    // ===== TAG METHODS =====

    public ObservableList<String> getAvailableTags() {
        return availableTags;
    }

    public ObservableList<String> getSelectedTags() {
        return selectedTags;
    }

    /**
     * Retorna uma lista de strings com as tags selecionadas
     * Para usar ao gerar sugestões
     */
    public List<String> getSelectedTagsList() {
        return new ArrayList<>(selectedTags);
    }

    /**
     * Adiciona uma nova tag (custom do utilizador)
     */
    public void addTag(String tag) {
        if (tag == null || tag.trim().isEmpty()) {
            logger.warn("Cannot add empty tag");
            return;
        }

        String normalizedTag = tag.trim().toLowerCase();

        if (!availableTags.contains(normalizedTag)) {
            availableTags.add(normalizedTag);
            selectedTags.add(normalizedTag);
            logger.info("Tag added: {}", normalizedTag);
        } else {
            logger.warn("Tag already exists: {}", normalizedTag);
        }
    }

    /**
     * Remove uma tag das disponíveis e selecionadas
     */
    public void removeTag(String tag) {
        availableTags.remove(tag);
        selectedTags.remove(tag);
        logger.info("Tag removed: {}", tag);
    }

    /**
     * Toggle de uma tag (selecionar/desselecionar)
     */
    public void toggleTag(String tag) {
        if (selectedTags.contains(tag)) {
            selectedTags.remove(tag);
            logger.info("Tag deselected: {}", tag);
        } else {
            selectedTags.add(tag);
            logger.info("Tag selected: {}", tag);
        }
    }

    /**
     * Verifica se uma tag está selecionada
     */
    public boolean isTagSelected(String tag) {
        return selectedTags.contains(tag);
    }

    /**
     * Define todas as tags disponíveis (vindas das sugestões atuais)
     */
    public void setAvailableTags(List<String> tags) {
        availableTags.clear();
        availableTags.addAll(tags);

        // Inicialmente, todas estão selecionadas
        selectedTags.clear();
        selectedTags.addAll(tags);

        logger.info("Available tags set: {} tags", tags.size());
    }

    /**
     * Limpa todas as tags selecionadas
     */
    public void clearSelectedTags() {
        selectedTags.clear();
        logger.info("All tags cleared");
    }

    /**
     * Seleciona todas as tags
     */
    public void selectAllTags() {
        selectedTags.clear();
        selectedTags.addAll(availableTags);
        logger.info("All tags selected");
    }

    // ===== BUDGET METHODS =====

    private void setupBidirectionalBinding() {
        budgetValue.addListener((observable, oldValue, newValue) -> {
            budgetText.set(String.format("%.0f €", newValue.doubleValue()));
            logger.debug("Budget value changed to: {}", newValue.doubleValue());
        });

        budgetText.addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                try {
                    String numericValue = newValue.replace("€", "").trim();
                    double value = Double.parseDouble(numericValue);
                    value = Math.max(MIN_BUDGET, Math.min(MAX_BUDGET, value));

                    if (Math.abs(budgetValue.get() - value) > 0.01) {
                        budgetValue.set(value);
                    }
                } catch (NumberFormatException e) {
                    budgetText.set(String.format("%.0f €", budgetValue.get()));
                    logger.warn("Invalid budget input: {}", newValue);
                }
            }
        });
    }

    public DoubleProperty budgetValueProperty() {
        return budgetValue;
    }

    public StringProperty budgetTextProperty() {
        return budgetText;
    }

    public double getBudgetValue() {
        return budgetValue.get();
    }

    public void setBudgetValue(double value) {
        if (value >= MIN_BUDGET && value <= MAX_BUDGET) {
            budgetValue.set(value);
            logger.info("Budget set to: {}", value);
        } else {
            logger.warn("Invalid budget value: {}. Must be between {} and {}",
                    value, MIN_BUDGET, MAX_BUDGET);
        }
    }

    // ===== TRAVEL STYLE METHODS =====

    public ObservableList<String> getTravelStyles() {
        return travelStyles;
    }

    public StringProperty selectedTravelStyleProperty() {
        return selectedTravelStyle;
    }

    public String getSelectedTravelStyle() {
        return selectedTravelStyle.get();
    }

    public String getSelectedTravelCompany() {
        return selectedTravelCompany.get();
    }

    public String getSelectedActivityLevel() {
        return selectedActivityLevel.get();
    }

    public void setSelectedTravelStyle(String style) {
        if (travelStyles.contains(style)) {
            selectedTravelStyle.set(style);
            logger.info("Travel style selected: {}", style);
        } else {
            logger.warn("Invalid travel style: {}", style);
        }
    }

    public void setSelectedTravelCompany(String company) {
        if (travelCompanyPreferences.contains(company)) {
            selectedTravelCompany.set(company);
            logger.info("Travel company selected: {}", company);
        } else {
            logger.warn("Invalid travel company: {}", company);
        }
    }

    public void setSelectedActivityLevel(String level) {
        if (activityLevelPreferences.contains(level)) {
            selectedActivityLevel.set(level);
            logger.info("Activity level selected: {}", level);
        } else {
            logger.warn("Invalid activity level: {}", level);
        }
    }

    // ===== WEATHER METHODS =====

    public ObservableList<String> getWeatherPreferences() {
        return weatherPreferences;
    }

    public StringProperty selectedWeatherProperty() {
        return selectedWeather;
    }

    public String getSelectedWeather() {
        return selectedWeather.get();
    }

    public void setSelectedWeather(String weather) {
        if (weatherPreferences.contains(weather)) {
            selectedWeather.set(weather);
            logger.info("Weather preference selected: {}", weather);
        } else {
            logger.warn("Invalid weather preference: {}", weather);
        }
    }

    // ===== VALIDATION METHODS =====

    public boolean isValidInput(String input) {
        return input != null && input.matches("[0-9]*");
    }

    public boolean arePreferencesComplete() {
        boolean complete = budgetValue.get() > 0 &&
                !selectedTravelStyle.get().isEmpty() &&
                !selectedWeather.get().isEmpty();

        logger.debug("Preferences complete: {}", complete);
        return complete;
    }

    public void resetToDefaults() {
        budgetValue.set(DEFAULT_BUDGET);
        selectedTravelStyle.set("");
        selectedWeather.set("");
        logger.info("Preferences reset to defaults");
    }

    public String getPreferencesSummary() {
        return String.format(
                "Budget: %.0f€ | Travel Style: %s | Weather: %s | Selected Tags: %d",
                budgetValue.get(),
                selectedTravelStyle.get().isEmpty() ? "Not set" : selectedTravelStyle.get(),
                selectedWeather.get().isEmpty() ? "Not set" : selectedWeather.get(),
                selectedTags.size()
        );
    }
}