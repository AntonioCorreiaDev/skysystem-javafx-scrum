package pt.isec.skysystem.view.pages;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.isec.skysystem.model.DataFacade;
import pt.isec.skysystem.model.data.TravelPreferences;
import pt.isec.skysystem.viewmodel.InspireMeViewModel;
import pt.isec.skysystem.viewmodel.PreferencesViewModel;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class PreferencesView implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(PreferencesView.class);

    private PreferencesViewModel viewModel;

    @FXML private Slider budgetSlider;
    @FXML private AnchorPane BudgetLabel;
    @FXML private HBox btnMountain;
    @FXML private HBox btnCity;
    @FXML private HBox btnBeach;
    @FXML private HBox btnCountrySide;
    @FXML private HBox btnHot;
    @FXML private HBox btnWarm;
    @FXML private HBox btnNoPreferences;
    @FXML private HBox btnSnowy;
    @FXML private HBox btnSolo;
    @FXML private HBox btnFriends;
    @FXML private HBox btnCouple;
    @FXML private HBox btnFamily;
    @FXML private HBox btnModerate;
    @FXML private HBox btnHigh;
    @FXML private HBox btnRelaxed;
    @FXML private Button backButton;
    @FXML private FlowPane tagsContainer;
    @FXML private TextField newTagField;
    @FXML private Button addTagButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.viewModel = new PreferencesViewModel();
        setupBudgetSlider();
        loadExistingPreferences();
        populateTags();

        // NOVO: Carregar as tags personalizadas salvas anteriormente
        loadSavedCustomTags();

        logger.info("PreferencesView initialized");
    }

    /**
     * Carrega as tags personalizadas que foram salvas no DataFacade
     */
    private void loadSavedCustomTags() {
        if (tagsContainer == null) {
            logger.error("tagsContainer is null!");
            return;
        }

        // Limpar container antes de popular
        tagsContainer.getChildren().clear();

        try {
            List<String> savedTags = DataFacade.getInstance().getCustomTags();

            if (savedTags != null && !savedTags.isEmpty()) {
                logger.info("Loading {} previously saved custom tags", savedTags.size());

                // Adicionar todas as tags salvas ao ViewModel
                for (String tag : savedTags) {
                    if (!viewModel.getAvailableTags().contains(tag)) {
                        // Tag nova (adicionada pelo user)
                        viewModel.getAvailableTags().add(tag);
                    }
                    // Garantir que está selecionada
                    if (!viewModel.getSelectedTags().contains(tag)) {
                        viewModel.getSelectedTags().add(tag);
                    }
                }

                logger.info("Custom tags loaded and synchronized");
            } else {
                logger.info("No saved custom tags found");
            }

            // Agora criar os botões para TODAS as tags disponíveis
            for (String tag : viewModel.getAvailableTags()) {
                boolean isSelected = viewModel.isTagSelected(tag);
                addTagButtonToUI(tag, isSelected);
            }

            logger.info("Created tag buttons for {} tags", viewModel.getAvailableTags().size());

        } catch (Exception e) {
            logger.error("Error loading saved custom tags", e);
            e.printStackTrace();
        }
    }

    /**
     * Atualiza a UI das tags para refletir o estado atual do ViewModel
     */
    private void updateTagsUI() {
        // Recriar todos os botões de tag com o estado correto
        tagsContainer.getChildren().clear();

        for (String tag : viewModel.getAvailableTags()) {
            boolean isSelected = viewModel.isTagSelected(tag);
            addTagButtonToUI(tag, isSelected);
        }
    }

    private void setupBudgetSlider() {
        if (budgetSlider == null || BudgetLabel == null) {
            logger.error("Budget slider or label is null!");
            return;
        }

        budgetSlider.valueProperty().bindBidirectional(viewModel.budgetValueProperty());

        Label valueLabel = new Label();
        valueLabel.textProperty().bind(viewModel.budgetTextProperty());
        valueLabel.setStyle("-fx-text-fill: #2C5F6F; -fx-font-size: 11px; -fx-font-weight: bold;");

        AnchorPane.setTopAnchor(valueLabel, 0.0);
        AnchorPane.setBottomAnchor(valueLabel, 0.0);
        AnchorPane.setLeftAnchor(valueLabel, 0.0);
        AnchorPane.setRightAnchor(valueLabel, 0.0);
        valueLabel.setAlignment(javafx.geometry.Pos.CENTER);

        BudgetLabel.getChildren().add(valueLabel);
        budgetSlider.setValue(2000.0);

        logger.info("Budget slider configured successfully");
    }

    /**
     * Popula as tags a partir das sugestões existentes
     */
    private void populateTags() {
        if (tagsContainer == null) {
            logger.error("tagsContainer is null!");
            return;
        }

        // NÃO limpar aqui, porque vamos adicionar as tags salvas depois
        // tagsContainer.getChildren().clear();

        // Obter tags únicas das sugestões ATUAIS
        InspireMeViewModel inspireMeVM = new InspireMeViewModel();
        List<String> allTags = inspireMeVM.getAllUniqueTags();

        if (allTags != null && !allTags.isEmpty()) {
            viewModel.setAvailableTags(allTags);
            logger.info("Populated {} tags from current suggestions", allTags.size());
        } else {
            logger.warn("No tags found in current suggestions");
        }

        // Não adicionar os botões aqui - será feito em loadSavedCustomTags()
    }

    /**
     * Adiciona um botão de tag na UI
     */
    private void addTagButtonToUI(String tag, boolean selected) {
        HBox tagBox = new HBox(5);
        tagBox.setAlignment(javafx.geometry.Pos.CENTER);
        tagBox.setStyle(selected
                ? "-fx-background-color: #72CCD6; -fx-background-radius: 15; -fx-padding: 5 10 5 10; -fx-cursor: hand;"
                : "-fx-background-color: #E0E0E0; -fx-background-radius: 15; -fx-padding: 5 10 5 10; -fx-cursor: hand;");

        Label tagLabel = new Label(tag);
        tagLabel.setStyle("-fx-text-fill: #2C5F6F; -fx-font-size: 10px;");

        // Botão para remover tag
        Label removeBtn = new Label("×");
        removeBtn.setStyle("-fx-text-fill: #2C5F6F; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;");

        // IMPORTANTE: Consumir o evento para não propagar para o HBox
        removeBtn.setOnMouseClicked(e -> {
            e.consume(); // ← ISTO É CRÍTICO!
            viewModel.removeTag(tag);
            tagsContainer.getChildren().remove(tagBox);
            logger.info("Tag removed from UI: {}", tag);
        });

        tagBox.getChildren().addAll(tagLabel, removeBtn);

        // Click na tag para selecionar/desselecionar (mas não no removeBtn)
        tagBox.setOnMouseClicked(e -> {
            // Só processar se não foi no botão de remover
            if (e.getTarget() != removeBtn && e.getTarget() != removeBtn.getGraphic()) {
                viewModel.toggleTag(tag);
                boolean isSelected = viewModel.isTagSelected(tag);
                tagBox.setStyle(isSelected
                        ? "-fx-background-color: #72CCD6; -fx-background-radius: 15; -fx-padding: 5 10 5 10; -fx-cursor: hand;"
                        : "-fx-background-color: #E0E0E0; -fx-background-radius: 15; -fx-padding: 5 10 5 10; -fx-cursor: hand;");
                logger.info("Tag toggled: {} -> selected: {}", tag, isSelected);
            }
        });

        tagsContainer.getChildren().add(tagBox);
    }

    /**
     * Adiciona uma nova tag custom
     */
    @FXML
    private void handleAddTag() {
        String tag = newTagField.getText().trim();
        if (tag.isEmpty()) {
            return;
        }

        String normalizedTag = tag.toLowerCase();

        if (!viewModel.getAvailableTags().contains(normalizedTag)) {
            viewModel.addTag(normalizedTag);
            addTagButtonToUI(normalizedTag, true);
            logger.info("Custom tag added: {}", normalizedTag);
        }

        newTagField.clear();
    }
    @FXML
    private void handleBackButton() {
        logger.info("Back button clicked");

        try {
            List<String> selectedTags = viewModel.getSelectedTagsList();
            DataFacade.getInstance().setCustomTags(selectedTags);
            logger.info("Saved {} custom tags to DataFacade: {}", selectedTags.size(), selectedTags);

            if (arePreferencesComplete()) {
                savePreferences();
                logger.info("Preferences saved successfully");
            } else {
                logger.info("Preferences incomplete, returning without saving full preferences");
            }

            Parent homePageRoot = FXMLLoader.load(
                    Objects.requireNonNull(getClass().getResource("/fxml/ai_suggestions.fxml"))
            );

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.getScene().setRoot(homePageRoot);

            logger.info("Successfully returned to suggestions page");

        } catch (IOException e) {
            logger.error("Failed to load ai_suggestions.fxml", e);
            e.printStackTrace();
        } catch (Exception e) {
            logger.error("Unexpected error returning to suggestions", e);
            e.printStackTrace();
        }
    }

    /**
     * Verifica se as preferências estão completas
     */
    private boolean arePreferencesComplete() {
        return viewModel.getBudgetValue() > 0 &&
                viewModel.getSelectedWeather() != null && !viewModel.getSelectedWeather().isEmpty() &&
                viewModel.getSelectedTravelCompany() != null && !viewModel.getSelectedTravelCompany().isEmpty() &&
                viewModel.getSelectedActivityLevel() != null && !viewModel.getSelectedActivityLevel().isEmpty() &&
                viewModel.getSelectedTravelStyle() != null && !viewModel.getSelectedTravelStyle().isEmpty();
    }
    public void setLastPage(String s){

    }

    private void loadExistingPreferences() {
        TravelPreferences prefs = DataFacade.getInstance().getCurrentUserPreferences();

        if (prefs == null) return;

        if (prefs.getBudgetLevel() != null) {
            viewModel.setBudgetValue(prefs.getBudgetLevel().getMinDaily());
        }

        if (prefs.getVibe() != null) {
            String style = prefs.getVibe().toUI();
            selectTravelStyleButton(style);
            viewModel.setSelectedTravelStyle(style);
        }

        if (prefs.getClimate() != null) {
            String weather = prefs.getClimate().toUI();
            selectWeatherButton(weather);
            viewModel.setSelectedWeather(weather);
        }

        if (prefs.getCompany() != null) {
            String company = prefs.getCompany().toUI();
            selectCompanyButton(company);
            viewModel.setSelectedTravelCompany(company);
        }

        if (prefs.getPace() != null) {
            String pace = prefs.getPace().toUI();
            selectActivityButton(pace);
            viewModel.setSelectedActivityLevel(pace);
        }

        logger.info("Existing preferences loaded into UI");
    }

    // ===== TRAVEL STYLE =====
    @FXML
    private void handleTravelStyleClick(MouseEvent event) {
        HBox clicked = (HBox) event.getSource();
        String style = ((Label) clicked.getChildren().get(1)).getText();
        viewModel.setSelectedTravelStyle(style);
        updateTravelStyleSelection(clicked);
    }

    private void updateTravelStyleSelection(HBox selected) {
        List<HBox> buttons = List.of(btnMountain, btnCity, btnBeach, btnCountrySide);
        for (HBox hb : buttons) {
            hb.setStyle(hb == selected
                    ? "-fx-background-color: #72CCD6; -fx-background-radius: 5; -fx-border-color: #2C5F6F; -fx-border-radius: 5; -fx-cursor: hand;"
                    : "-fx-background-color: #FFFFFF; -fx-background-radius: 5; -fx-border-color: #72CCD6; -fx-border-radius: 5; -fx-cursor: hand;");
        }
    }

    private void selectTravelStyleButton(String style) {
        if (style == null) return;
        switch (style) {
            case "Mountain" -> updateTravelStyleSelection(btnMountain);
            case "City" -> updateTravelStyleSelection(btnCity);
            case "Beach" -> updateTravelStyleSelection(btnBeach);
            case "CountrySide" -> updateTravelStyleSelection(btnCountrySide);
        }
    }

    // ===== WEATHER =====
    @FXML
    private void handleWeatherClick(MouseEvent event) {
        HBox clicked = (HBox) event.getSource();
        String weather = ((Label) clicked.getChildren().get(1)).getText();
        viewModel.setSelectedWeather(weather);
        updateWeatherSelection(clicked);
    }

    private void updateWeatherSelection(HBox selected) {
        List<HBox> buttons = List.of(btnHot, btnWarm, btnNoPreferences, btnSnowy);
        for (HBox hb : buttons) {
            hb.setStyle(hb == selected
                    ? "-fx-background-color: #72CCD6; -fx-background-radius: 5; -fx-border-color: #2C5F6F; -fx-border-radius: 5; -fx-cursor: hand;"
                    : "-fx-background-color: #FFFFFF; -fx-background-radius: 5; -fx-border-color: #72CCD6; -fx-border-radius: 5; -fx-cursor: hand;");
        }
    }

    private void selectWeatherButton(String weather) {
        if (weather == null) return;
        switch (weather) {
            case "Hot" -> updateWeatherSelection(btnHot);
            case "Warm" -> updateWeatherSelection(btnWarm);
            case "No Preferences" -> updateWeatherSelection(btnNoPreferences);
            case "Snowy" -> updateWeatherSelection(btnSnowy);
        }
    }

    // ===== COMPANY =====
    @FXML
    private void handleTravelCompanyClick(MouseEvent event) {
        HBox clicked = (HBox) event.getSource();
        String company = ((Label) clicked.getChildren().get(1)).getText();
        viewModel.setSelectedTravelCompany(company);
        updateTravelCompanySelection(clicked);
    }

    private void updateTravelCompanySelection(HBox selected) {
        List<HBox> buttons = List.of(btnSolo, btnFriends, btnCouple, btnFamily);
        for (HBox hb : buttons) {
            hb.setStyle(hb == selected
                    ? "-fx-background-color: #72CCD6; -fx-background-radius: 5; -fx-border-color: #2C5F6F; -fx-border-radius: 5; -fx-cursor: hand;"
                    : "-fx-background-color: #FFFFFF; -fx-background-radius: 5; -fx-border-color: #72CCD6; -fx-border-radius: 5; -fx-cursor: hand;");
        }
    }

    private void selectCompanyButton(String company) {
        if (company == null) return;
        switch (company) {
            case "Solo" -> updateTravelCompanySelection(btnSolo);
            case "Friends" -> updateTravelCompanySelection(btnFriends);
            case "Couple" -> updateTravelCompanySelection(btnCouple);
            case "Family" -> updateTravelCompanySelection(btnFamily);
        }
    }

    // ===== ACTIVITY LEVEL =====
    @FXML
    private void handleActivityLevelClick(MouseEvent event) {
        HBox clicked = (HBox) event.getSource();
        String level = ((Label) clicked.getChildren().get(1)).getText();
        viewModel.setSelectedActivityLevel(level);
        updateActivityLevelSelection(clicked);
    }

    private void updateActivityLevelSelection(HBox selected) {
        List<HBox> buttons = List.of(btnRelaxed, btnModerate, btnHigh);
        for (HBox hb : buttons) {
            hb.setStyle(hb == selected
                    ? "-fx-background-color: #72CCD6; -fx-background-radius: 5; -fx-border-color: #2C5F6F; -fx-border-radius: 5; -fx-cursor: hand;"
                    : "-fx-background-color: #FFFFFF; -fx-background-radius: 5; -fx-border-color: #72CCD6; -fx-border-radius: 5; -fx-cursor: hand;");
        }
    }

    private void selectActivityButton(String pace) {
        if (pace == null) return;
        switch (pace) {
            case "Relaxed" -> updateActivityLevelSelection(btnRelaxed);
            case "Moderate" -> updateActivityLevelSelection(btnModerate);
            case "High" -> updateActivityLevelSelection(btnHigh);
        }
    }

    // ===== UTILS =====
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public double getBudgetValue() {
        return viewModel.getBudgetValue();
    }

    public void setBudgetValue(double value) {
        viewModel.setBudgetValue(value);
    }

    // Substituir o método savePreferences() na PreferencesView.java

    private void savePreferences() {
        try {
            TravelPreferences prefs = new TravelPreferences();

            // ===== BUDGET =====
            double budgetValue = viewModel.getBudgetValue();
            if (budgetValue > 0) {
                prefs.setBudgetLevel(TravelPreferences.BudgetLevel.fromDaily(budgetValue));
            } else {
                logger.warn("Budget not set, skipping");
                return; // Não salvar se não tiver budget
            }

            // ===== CLIMATE/WEATHER =====
            String selectedWeather = viewModel.getSelectedWeather();
            if (selectedWeather != null && !selectedWeather.isEmpty()) {
                try {
                    // Converter de UI para Enum
                    TravelPreferences.ClimatePreference climate = convertWeatherToEnum(selectedWeather);
                    prefs.setClimate(climate);
                } catch (IllegalArgumentException e) {
                    logger.error("Invalid weather selection: {}", selectedWeather);
                    showError("Selecione uma preferência de clima válida!");
                    return;
                }
            } else {
                logger.warn("Weather not selected");
                showError("Por favor, selecione uma preferência de clima!");
                return;
            }

            // ===== COMPANY =====
            String selectedCompany = viewModel.getSelectedTravelCompany();
            if (selectedCompany != null && !selectedCompany.isEmpty()) {
                try {
                    TravelPreferences.TravelCompany company = convertCompanyToEnum(selectedCompany);
                    prefs.setCompany(company);
                } catch (IllegalArgumentException e) {
                    logger.error("Invalid company selection: {}", selectedCompany);
                    showError("Selecione uma companhia de viagem válida!");
                    return;
                }
            } else {
                logger.warn("Company not selected");
                showError("Por favor, selecione com quem vai viajar!");
                return;
            }

            // ===== ACTIVITY LEVEL =====
            String selectedActivity = viewModel.getSelectedActivityLevel();
            if (selectedActivity != null && !selectedActivity.isEmpty()) {
                try {
                    TravelPreferences.TravelPace pace = convertActivityToEnum(selectedActivity);
                    prefs.setPace(pace);
                } catch (IllegalArgumentException e) {
                    logger.error("Invalid activity level selection: {}", selectedActivity);
                    showError("Selecione um nível de atividade válido!");
                    return;
                }
            } else {
                logger.warn("Activity level not selected");
                showError("Por favor, selecione um nível de atividade!");
                return;
            }

            // ===== TRAVEL STYLE =====
            String selectedStyle = viewModel.getSelectedTravelStyle();
            if (selectedStyle != null && !selectedStyle.isEmpty()) {
                try {
                    TravelPreferences.Vibe vibe = convertStyleToEnum(selectedStyle);
                    prefs.setVibe(vibe);
                } catch (IllegalArgumentException e) {
                    logger.error("Invalid travel style selection: {}", selectedStyle);
                    showError("Selecione um estilo de viagem válido!");
                    return;
                }
            } else {
                logger.warn("Travel style not selected");
                showError("Por favor, selecione um estilo de viagem!");
                return;
            }

            // Salvar as preferências
            boolean saved = DataFacade.getInstance().saveUserPreferences(prefs);

            if (saved) {
                logger.info("Preferences saved successfully with {} selected tags",
                        viewModel.getSelectedTags().size());
            } else {
                logger.error("Failed to save preferences");
                showError("Erro ao salvar preferências!");
            }

        } catch (Exception e) {
            logger.error("Unexpected error saving preferences", e);
            showError("Erro inesperado ao salvar: " + e.getMessage());
        }
    }

// ===== MÉTODOS AUXILIARES DE CONVERSÃO =====

    /**
     * Converte a seleção de Weather (UI) para ClimatePreference (Enum)
     */
    private TravelPreferences.ClimatePreference convertWeatherToEnum(String weather) {
        return switch (weather) {
            case "Hot" -> TravelPreferences.ClimatePreference.HOT_SUNNY;
            case "Warm" -> TravelPreferences.ClimatePreference.WARM_MILD;
            case "Snowy" -> TravelPreferences.ClimatePreference.COOL_CRISP;
            case "No Preferences" -> TravelPreferences.ClimatePreference.NO_PREFERENCE;
            default -> throw new IllegalArgumentException("Unknown weather: " + weather);
        };
    }

    /**
     * Converte a seleção de Company (UI) para TravelCompany (Enum)
     */
    private TravelPreferences.TravelCompany convertCompanyToEnum(String company) {
        return switch (company) {
            case "Solo" -> TravelPreferences.TravelCompany.SOLO;
            case "Friends" -> TravelPreferences.TravelCompany.FRIENDS;
            case "Couple" -> TravelPreferences.TravelCompany.PARTNER;
            case "Family" -> TravelPreferences.TravelCompany.FAMILY;
            default -> throw new IllegalArgumentException("Unknown company: " + company);
        };
    }

    /**
     * Converte a seleção de Activity Level (UI) para TravelPace (Enum)
     */
    private TravelPreferences.TravelPace convertActivityToEnum(String activity) {
        return switch (activity) {
            case "Relaxed" -> TravelPreferences.TravelPace.RELAXED;
            case "Moderate" -> TravelPreferences.TravelPace.BALANCED;
            case "High" -> TravelPreferences.TravelPace.ACTION_PACKED;
            default -> throw new IllegalArgumentException("Unknown activity: " + activity);
        };
    }

    /**
     * Converte a seleção de Travel Style (UI) para Vibe (Enum)
     */
    private TravelPreferences.Vibe convertStyleToEnum(String style) {
        return switch (style) {
            case "Mountain" -> TravelPreferences.Vibe.MOUNTAIN_ADVENTURE;
            case "City" -> TravelPreferences.Vibe.CITY_CULTURE;
            case "Beach" -> TravelPreferences.Vibe.BEACH_SUN;
            case "CountrySide" -> TravelPreferences.Vibe.NATURE_WILDLIFE;
            default -> throw new IllegalArgumentException("Unknown style: " + style);
        };
    }


}