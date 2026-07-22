package pt.isec.skysystem.model.data.Controllers;

import javafx.animation.RotateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import pt.isec.skysystem.model.DataFacade;
import pt.isec.skysystem.model.data.Suggestion;
import pt.isec.skysystem.view.pages.InspireMeView;

public class SuggestionController {

    @FXML private ImageView cardImage;
    @FXML private Label cardDestination;
    @FXML private Label cardTags;
    @FXML private Label cardReason;
    @FXML private Label cardPrice;
    @FXML private Button refreshCardButton;
    @FXML private Button favouriteCardButton;

    private InspireMeView parentView;
    private Suggestion currentSuggestion;

    private boolean isFavorite = false;

    private final Image heartIcon = new Image(getClass().getResourceAsStream("/images/icons/heart.png"));
    private final Image filledHeartIcon = new Image(getClass().getResourceAsStream("/images/icons/filled-heart.png"));

    public void setData(Suggestion suggestion, InspireMeView parentView) {
        this.currentSuggestion = suggestion;
        this.parentView = parentView;

        if (suggestion != null) {
            // Bindings de texto
            if (cardDestination != null) cardDestination.textProperty().bind(suggestion.destinationProperty());
            if (cardTags != null) cardTags.textProperty().bind(suggestion.tagsProperty());
            if (cardReason != null) cardReason.textProperty().bind(suggestion.reasonProperty());
            if (cardPrice != null) cardPrice.textProperty().bind(suggestion.priceRangeProperty());

            // Imagem do destino
            if (cardImage != null) {
                updateImage(suggestion.getImageUrl());
                suggestion.imageUrlProperty().addListener((obs, oldVal, newVal) -> updateImage(newVal));
            }

            // 1. VERIFICAR ESTADO INICIAL
            this.isFavorite = DataFacade.getInstance().isFavoriteDestination(suggestion.getDestination());

            // 2. APLICAR O VISUAL (Opacidade + Fundo)
            updateHeartVisual();

        } else {
            if (cardDestination != null) cardDestination.setText("Error");
        }
    }

    @FXML
    private void handleFavouriteCardButton() {
        if (currentSuggestion == null) return;

        // 1. Inverter o estado
        isFavorite = !isFavorite;

        // 2. Atualizar a Base de Dados
        if (isFavorite) {
            DataFacade.getInstance().addFavouriteDestination(
                    currentSuggestion.getDestination(),
                    currentSuggestion.getImageUrl(),
                    currentSuggestion.getTags(),
                    currentSuggestion.getReason(),
                    currentSuggestion.getPriceRange());
        } else {
            DataFacade.getInstance().removeFavoriteDestination(
                    currentSuggestion.getDestination(),
                    currentSuggestion.getImageUrl());
        }

        // 3. Atualizar o visual
        updateHeartVisual();
    }

    private void updateHeartVisual() {
        if (favouriteCardButton != null) {
            ImageView iconView = (ImageView) favouriteCardButton.getGraphic();

            if (iconView != null) {
                if (isFavorite) {
                    iconView.setImage(filledHeartIcon);
                } else {
                    iconView.setImage(heartIcon);
                }
            }
            favouriteCardButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        }
    }

    private void updateImage(String url) {
        if (url != null && !url.isEmpty()) {
            try {
                Image image = new Image(url, true);
                cardImage.setImage(image);
                Rectangle clip = new Rectangle(cardImage.getFitWidth(), cardImage.getFitHeight());
                clip.setArcWidth(10);
                clip.setArcHeight(10);
                cardImage.setClip(clip);
            } catch (Exception e) {
                System.err.println("Error loading image: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleRefreshCardButton() {
        if (parentView != null && currentSuggestion != null) {
            RotateTransition rotate = new RotateTransition(Duration.millis(1000), refreshCardButton);
            rotate.setByAngle(360);
            rotate.setCycleCount(RotateTransition.INDEFINITE);
            rotate.play();
            refreshCardButton.setDisable(true);
            parentView.refreshSingleSuggestion(currentSuggestion);
        }
    }

    @FXML
    private void handleSearchCardButton() {
        if (parentView != null && currentSuggestion != null && cardDestination != null) {
            String fullText = this.cardDestination.getText();
            if (fullText != null && !fullText.isEmpty()) {
                String firstWord = fullText.split(",")[0].replace(",", "");
                parentView.searchFlight(firstWord);
            }
        }
    }
}