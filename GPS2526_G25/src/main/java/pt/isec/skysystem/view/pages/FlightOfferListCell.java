package pt.isec.skysystem.view.pages;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListCell;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import pt.isec.skysystem.model.data.flights.Trip;
import java.io.IOException;

/**
 * Custom ListCell para exibir o card de cada Voo.
 */
public class FlightOfferListCell extends ListCell<Trip> {

    private VBox cardRoot;
    private FlightCardController cardController;
    private FXMLLoader mLLoader;

    private static final String FXML_PATH = "/fxml/FlightDetails.fxml";

    @Override
    protected void updateItem(Trip trip, boolean empty) {
        super.updateItem(trip, empty);

        if (empty || trip == null) {
            setText(null);
            setGraphic(null);
        } else {
            if (mLLoader == null) {
                mLLoader = new FXMLLoader(getClass().getResource(FXML_PATH));

                try {
                    mLLoader.load();
                    cardController = mLLoader.getController();
                    cardRoot = mLLoader.getRoot();
                } catch (IOException e) {
                    e.printStackTrace();
                    setGraphic(new Label("ERROR: Failed to load FXML. Verify the path: " + FXML_PATH
                            + " \nDetails: " + e.getMessage()));
                    return;
                }
            }

            if (cardController != null) {
                cardController.setFlightData(trip);
            }
            setGraphic(cardRoot);

            setStyle("-fx-background-color: transparent; -fx-padding: 5;");
        }
    }
}