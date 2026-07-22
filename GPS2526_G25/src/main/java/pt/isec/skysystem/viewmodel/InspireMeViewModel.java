package pt.isec.skysystem.viewmodel;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.isec.skysystem.model.DataFacade;
import pt.isec.skysystem.model.data.Suggestion;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.application.Platform;
import java.util.concurrent.CompletableFuture;

public class InspireMeViewModel {

    private final ObservableList<Suggestion> suggestionsList;
    private static final Logger logger = LoggerFactory.getLogger(InspireMeViewModel.class);
    private final DataFacade facade;
    private final BooleanProperty isLoading = new SimpleBooleanProperty(false);
    private final String pageName = "ai_suggestions";
    public InspireMeViewModel() {
        this.facade = DataFacade.getInstance();
        this.suggestionsList = FXCollections.observableArrayList(facade.getLastAiSuggestions());
    }

    public ObservableList<Suggestion> getSuggestionsList() {
        return suggestionsList;
    }

    public BooleanProperty isLoadingProperty() {
        return isLoading;
    }

    /**
     * Substitui as sugestões atuais por novas, excluindo os destinos atuais.
     */
    public void refreshAllSuggestions() {
        if (isLoading.get())
            return;
        isLoading.set(true);
        logger.info("Loading new suggestions...");
        CompletableFuture.runAsync(() -> {
            List<String> excludeList = suggestionsList.stream()
                    .map(s -> s.destinationProperty().get())
                    .collect(Collectors.toList());
            try {
                var suggestions = facade.getAiSuggestions(excludeList);

                Platform.runLater(() -> {
                    suggestionsList.clear();
                    suggestionsList.addAll(suggestions);
                    logger.info("Loaded {} new suggestions", suggestions.size());
                    isLoading.set(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    logger.error("Error loading all new suggestions: " + e.getMessage());
                    isLoading.set(false);
                });
            }
        });
    }

    /**
     * Substitui UMA sugestão por 1 nova, excluindo as outras todas.
     */
    public void refreshSingleSuggestion(Suggestion suggestionToReplace) {
        logger.info("Refreshing single suggestion: {}", suggestionToReplace.destinationProperty().get());

        CompletableFuture.runAsync(() -> {
            List<String> excludeList = suggestionsList.stream()
                    .map(s -> s.destinationProperty().get())
                    .collect(Collectors.toList());
            try {
                Suggestion newSuggestion = facade.getSingleNewSuggestion(excludeList);
                Platform.runLater(() -> {
                    if (newSuggestion != null) {
                        int index = suggestionsList.indexOf(suggestionToReplace);
                        if (index != -1) {
                            suggestionsList.set(index, newSuggestion);
                            facade.replaceStoredSuggestion(suggestionToReplace, newSuggestion);
                            logger.info("Replaced suggestion at index {} with {}", index,
                                    newSuggestion.destinationProperty().get());
                        }
                    } else {
                        logger.warn("Could not get a new single suggestion.");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> logger.error("Error refreshing single suggestion: " + e.getMessage()));
            }
        });
    }


    /**
     * Obtém todas as tags únicas de todas as sugestões
     */
    public List<String> getAllUniqueTags() {
        return suggestionsList.stream()
                .map(suggestion -> suggestion.tagsProperty().get()) // Obtém a string de tags
                .filter(tags -> tags != null && !tags.isEmpty()) // Filtra nulls e vazios
                .flatMap(tags -> Arrays.stream(tags.split(","))) // Divide por vírgula e cria stream
                .map(String::trim) // Remove espaços em branco
                .filter(tag -> !tag.isEmpty()) // Remove tags vazias
                .distinct() // Remove duplicados
                .sorted() // Ordena alfabeticamente
                .collect(Collectors.toList());
    }

    public void addFavourite(String destination, String imgUrl, String tags, String reason, String priceRange) {
        facade.addFavouriteDestination(destination, imgUrl, tags, reason, priceRange);
    }

    public void setFlightSearch(String s){
        facade.setFlightToSearch(s);
    }
    public void setLastPage(){
        facade.setLastPage(pageName);
    }
}