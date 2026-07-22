package pt.isec.skysystem.view.pages;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import pt.isec.skysystem.model.DataFacade;
import pt.isec.skysystem.model.data.flights.Trip;
import pt.isec.skysystem.viewmodel.DashBoardViewModel;
import pt.isec.skysystem.viewmodel.DashBoardViewModel.TripItem;

import java.io.IOException;
import java.util.List;

public class DashBoardView {

    private static DashBoardView currentInstance;

    @FXML
    private Label welcomeLabel;
    @FXML
    private FlowPane upcomingTripsContainer; // Favourite Destinations
    @FXML
    private VBox recommendedContainer; // Upcoming Flights (Saved Flights)
    @FXML
    private VBox travelHistoryContainer;
    @FXML
    private Button backButton;

    private DashBoardViewModel viewModel;

    public static void refreshUpcomingFlightsStatic() {
        if (currentInstance != null) {
            currentInstance.populateUpcomingFlights();
        }
    }

    @FXML
    public void initialize() {
        currentInstance = this;

        this.viewModel = new DashBoardViewModel();

        if (welcomeLabel != null)
            welcomeLabel.textProperty().bind(viewModel.welcomeMessageProperty());

        refresh();
    }

    @FXML
    public void refresh() {
        viewModel.refresh();
        populateFavorites();
        populateUpcomingFlights();
    }

    // ============================================================================================
    // SECTION: FAVOURITE DESTINATIONS (Small White Cards)
    // ============================================================================================

    private void populateFavorites() {
        if (upcomingTripsContainer == null)
            return;

        upcomingTripsContainer.getChildren().clear();

        for (TripItem trip : viewModel.getFavoriteTrips()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/FavouriteCard.fxml"));

                javafx.scene.layout.Pane cardNode = loader.load();

                SuggestionCardController controller = loader.getController();

                controller.setTripItemData(trip, () -> {

                    boolean success = viewModel.removeFavorite(trip.getName(), trip.getImagePath());

                    if (success) {
                        upcomingTripsContainer.getChildren().remove(cardNode);
                    }
                });

                upcomingTripsContainer.getChildren().add(cardNode);

            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Error loading FavouriteCard.fxml: " + e.getMessage());
            }
        }
    }

    private void populateUpcomingFlights() {
        if (recommendedContainer == null)
            return;

        recommendedContainer.getChildren().clear();

        List<Trip> savedFlights = DataFacade.getInstance().getSavedFlights();

        if (savedFlights.isEmpty()) {
            Label emptyLabel = new Label("No upcoming flights saved.");
            emptyLabel.setStyle("-fx-text-fill: #00333e; -fx-opacity: 0.6; -fx-font-size: 14px;");
            recommendedContainer.getChildren().add(emptyLabel);
            return;
        }

        for (Trip trip : savedFlights) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/FlightDetails.fxml"));
                Parent cardNode = loader.load();

                FlightCardController controller = loader.getController();
                controller.setFlightData(trip);

                recommendedContainer.getChildren().add(cardNode);

            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Error loading FlightDetails.fxml for dashboard.");
            }
        }
    }

    private VBox createCard(String imagePath, String mainText, String subText) {
        // Root Container
        VBox card = new VBox();
        card.setPrefSize(200, 220);
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 15; -fx-cursor: hand;");
        card.setAlignment(Pos.TOP_CENTER);

        // Drop Shadow Effect
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.15));
        shadow.setRadius(14.5);
        shadow.setWidth(30);
        shadow.setHeight(30);
        card.setEffect(shadow);

        // Image View
        ImageView imageView = new ImageView();
        imageView.setFitWidth(200);
        imageView.setFitHeight(160);
        imageView.setPreserveRatio(false);

        try {
            if (getClass().getResource(imagePath) != null) {
                imageView.setImage(new Image(getClass().getResource(imagePath).toExternalForm()));
            } else {
                imageView
                        .setImage(new Image(getClass().getResource("/images/placeholders/paris.jpg").toExternalForm()));
            }
        } catch (Exception e) {
            // Ignore
        }

        Rectangle clip = new Rectangle(200, 160);
        clip.setArcWidth(15);
        clip.setArcHeight(15);
        imageView.setClip(clip);

        // Text Area
        VBox textArea = new VBox(2);
        textArea.setAlignment(Pos.CENTER);
        textArea.setPadding(new Insets(10, 5, 10, 5));

        Label title = new Label(mainText);
        title.setTextFill(Color.web("#00333e"));
        title.setFont(Font.font("System", FontWeight.BOLD, 16));
        title.setWrapText(true);

        Label subtitle = new Label(subText);
        subtitle.setTextFill(Color.web("#00333e"));
        subtitle.setOpacity(0.6);
        subtitle.setFont(Font.font("System", 11));

        textArea.getChildren().addAll(title, subtitle);
        card.getChildren().addAll(imageView, textArea);

        return card;
    }

    // NAVIGATION
    @FXML
    private void handleBackButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/HomePage.fxml"));
            Parent root = loader.load();

            if (backButton.getScene() != null) {
                Stage stage = (Stage) backButton.getScene().getWindow();

                stage.getScene().setRoot(root);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(msg);
        alert.show();
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(msg);
        alert.show();
    }
}