package pt.isec.skysystem.view.pages;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import pt.isec.skysystem.viewmodel.DashBoardViewModel.TripItem;
import java.io.InputStream;

public class SuggestionCardController {

    @FXML private ImageView image;
    @FXML private Label destinationLabel;
    @FXML private Button removeButton;
    @FXML private VBox cardVBox;
    @FXML private VBox expandedDetailsSection;
    @FXML private Label reasonLabel;
    @FXML private Label priceLabel;

    private Runnable onRemoveCallback;
    private boolean isExpanded = false;
    private TripItem currentTrip;

    /**
     * Novo metodo que aceita o TripItem E a ação de remover (Callback)
     */
    public void setTripItemData(TripItem trip, Runnable onRemoveCallback) {
        this.onRemoveCallback = onRemoveCallback;
        this.currentTrip = trip;

        if (trip == null) return;

        if (destinationLabel != null) {
            destinationLabel.setText(trip.getName());
        }

        if (image != null) {
            loadTripImage(trip.getImagePath());

            Rectangle clip = new Rectangle(180, 140); // Ajuste ao tamanho da tua ImageView
            clip.setArcWidth(15);
            clip.setArcHeight(15);
            image.setClip(clip);
        }

        if (removeButton != null) {
            removeButton.setOnMouseEntered(e -> removeButton.setStyle("-fx-background-color: rgba(255,89,89,0.57); -fx-background-radius: 50; -fx-min-width: 25px; -fx-min-height: 25px; -fx-padding: 0; -fx-cursor: hand;"));
            removeButton.setOnMouseExited(e -> removeButton.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5); -fx-background-radius: 50; -fx-min-width: 25px; -fx-min-height: 25px; -fx-padding: 0; -fx-cursor: hand;"));
        }

        if (cardVBox != null) {
            cardVBox.setOnMouseClicked(event -> {
                if (removeButton != null
                        && removeButton.contains(removeButton.sceneToLocal(event.getSceneX(), event.getSceneY()))) {
                    return;
                }
                toggleExpansion();
            });
        }
    }

    @FXML
    private void handleRemoveAction() {
        if (onRemoveCallback != null) {
            System.out.println("Botão X clicado. A executar callback de remoção...");
            onRemoveCallback.run(); // Chama a lógica que está na DashBoardView
        }
    }

    /**
     * A tua lógica de carregar imagens (Mantida igual)
     */
    private void loadTripImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) return;

        if (imageUrl.contains("http://") || imageUrl.contains("https://")) {
            String cleanedUrl = imageUrl;
            int httpIndex = imageUrl.indexOf("http");
            if (httpIndex > 0) {
                cleanedUrl = imageUrl.substring(httpIndex);
            }
            if (cleanedUrl.endsWith(".jpg.jpg")) {
                cleanedUrl = cleanedUrl.substring(0, cleanedUrl.length() - 4);
            }

            try {
                Image img = new Image(cleanedUrl, true);
                image.setImage(img);
            } catch (Exception e) {
                loadDefaultLocalImage();
            }
        } else {
            try (InputStream imageStream = getClass().getResourceAsStream(imageUrl)) {
                if (imageStream != null) {
                    image.setImage(new Image(imageStream));
                } else {
                    loadDefaultLocalImage();
                }
            } catch (Exception e) {
                loadDefaultLocalImage();
            }
        }
    }

    private void loadDefaultLocalImage() {
        try {
            // Verifica se este caminho está correto no teu projeto
            InputStream defaultStream = getClass().getResourceAsStream("/images/placeholders/default.jpg");
            if (defaultStream != null) {
                image.setImage(new Image(defaultStream));
            }
        } catch (Exception e) {
            System.err.println("Falha ao carregar imagem default.");
        }
    }

    private void toggleExpansion() {
        if (expandedDetailsSection == null)
            return;

        isExpanded = !isExpanded;

        if (isExpanded) {
            populateExpandedDetails();
            expandedDetailsSection.setVisible(true);
            expandedDetailsSection.setManaged(true);
        } else {
            expandedDetailsSection.setVisible(false);
            expandedDetailsSection.setManaged(false);
        }
    }

    private void populateExpandedDetails() {
        if (currentTrip == null)
            return;

        if (reasonLabel != null) {
            String reason = currentTrip.getReason();
            reasonLabel.setText(reason != null && !reason.isEmpty() ? reason : "No description available");
        }

        if (priceLabel != null) {
            String price = currentTrip.getPriceRange();
            priceLabel.setText(price != null && !price.isEmpty() ? price : "€€");
        }
    }
}