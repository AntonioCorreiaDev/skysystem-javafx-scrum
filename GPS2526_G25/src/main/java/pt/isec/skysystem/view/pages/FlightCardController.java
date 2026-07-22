package pt.isec.skysystem.view.pages;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import pt.isec.skysystem.model.DataFacade;
import pt.isec.skysystem.model.data.TripNotification;
import pt.isec.skysystem.model.data.flights.Trip;
import pt.isec.skysystem.model.data.flights.Flight;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

public class FlightCardController {

    @FXML
    private VBox departureBox;
    @FXML
    private Label departureAirlineLabel;
    @FXML
    private Label departureTimeLabel;
    @FXML
    private Label departureAirportLabel;
    @FXML
    private Label departureDurationLabel;
    @FXML
    private Label departureStopsLabel;
    @FXML
    private Label arrivalTimeLabel;
    @FXML
    private Label arrivalAirportLabel;
    @FXML
    private Label departureDateLabel;
    @FXML
    private Label departureCityLabel;
    @FXML
    private Label arrivalCityLabel;

    @FXML
    private VBox returnBox;
    @FXML
    private Label returnAirlineLabel;
    @FXML
    private Label returnDepartureTimeLabel;
    @FXML
    private Label returnDepartureAirportLabel;
    @FXML
    private Label returnDurationLabel;
    @FXML
    private Label returnStopsLabel;
    @FXML
    private Label returnArrivalTimeLabel;
    @FXML
    private Label returnArrivalAirportLabel;
    @FXML
    private Label returnDepartureDateLabel;
    @FXML
    private Label returnDepartureCityLabel;
    @FXML
    private Label returnArrivalCityLabel;

    @FXML
    private Label priceLabel;
    @FXML
    private Button saveButton;
    @FXML
    private ImageView saveIcon;
    @FXML
    private Button detailsButton;
    @FXML
    private VBox expandedDetailsSection;

    // Expanded details labels
    @FXML
    private Label outboundFlightNumberLabel;
    @FXML
    private Label outboundAircraftLabel;
    @FXML
    private Label outboundClassLabel;
    @FXML
    private VBox returnDetailsBox;
    @FXML
    private Label returnFlightNumberLabel;
    @FXML
    private Label returnAircraftLabel;
    @FXML
    private Label returnClassLabel;
    @FXML
    private Label outboundAirlineNameLabel;
    @FXML
    private Label returnAirlineNameLabel;
    @FXML
    private ListView<TripNotification> historyListView;

    private Trip currentTrip;
    private boolean isExpanded = false;

    private final Image heartIcon = new Image(getClass().getResourceAsStream("/images/icons/heart.png"));
    private final Image filledHeartIcon = new Image(getClass().getResourceAsStream("/images/icons/filled-heart.png"));

    private String extractTime(String fullDateTimeString) {
        if (fullDateTimeString == null || fullDateTimeString.isEmpty() || !fullDateTimeString.contains("T")) {
            return "??:??";
        }
        try {
            String timePart = fullDateTimeString.split("T")[1];
            return timePart.substring(0, 5);
        } catch (Exception e) {
            System.err.println("Error extracting time from string: " + fullDateTimeString);
            return "Error Time";
        }
    }

    private String extractAndFormatDate(String fullDateTimeString) {
        if (fullDateTimeString == null || fullDateTimeString.isEmpty() || !fullDateTimeString.contains("T")) {
            return "";
        }
        try {
            LocalDateTime dateTime = LocalDateTime.parse(fullDateTimeString);

            String dayOfWeek = dateTime.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM");
            String datePart = dateTime.format(dateFormatter);

            return dayOfWeek + ", " + datePart;
        } catch (Exception e) {
            System.err.println("Error formatting date: " + fullDateTimeString);
            return "Invalid Date";
        }
    }

    public void setFlightData(Trip trip) {
        this.currentTrip = trip;
        if (trip == null)
            return;

        updateSaveButtonState();

        priceLabel.setText(String.format("%.2f €", trip.getTotalPrice()));

        Flight outbound = trip.getOutboundFlight();
        departureTimeLabel.setText(extractTime(outbound.getDepartureTime()));
        arrivalTimeLabel.setText(extractTime(outbound.getArrivalTime()));

        departureDateLabel.setText(extractAndFormatDate(outbound.getDepartureTime()));

        departureAirportLabel.setText(outbound.getOrigin());
        arrivalAirportLabel.setText(outbound.getDestination());

        departureCityLabel.setText(outbound.getOrigin());
        arrivalCityLabel.setText(outbound.getDestination());

        new Thread(() -> {
            String depCity = DataFacade.getInstance().getCityName(outbound.getOrigin());
            String arrCity = DataFacade.getInstance().getCityName(outbound.getDestination());
            javafx.application.Platform.runLater(() -> {
                departureCityLabel.setText(depCity);
                arrivalCityLabel.setText(arrCity);
            });
        }).start();

        departureDurationLabel.setText(outbound.getTotalDuration());

        int outboundStops = outbound.getNumberOfStops();
        departureStopsLabel.setText(outboundStops == 0 ? "Direct" : outboundStops + " stop(s)");

        departureAirlineLabel.setText("DEPARTURE • ");

        if (trip.isRoundTrip() && trip.getReturnFlight() != null) {
            returnBox.setVisible(true);
            returnBox.setManaged(true);

            Flight returnFlight = trip.getReturnFlight();
            returnDepartureTimeLabel.setText(extractTime(returnFlight.getDepartureTime()));
            returnArrivalTimeLabel.setText(extractTime(returnFlight.getArrivalTime()));

            returnDepartureDateLabel.setText(extractAndFormatDate(returnFlight.getDepartureTime()));

            returnDepartureAirportLabel.setText(returnFlight.getOrigin());
            returnArrivalAirportLabel.setText(returnFlight.getDestination());

            returnDepartureCityLabel.setText(returnFlight.getOrigin());
            returnArrivalCityLabel.setText(returnFlight.getDestination());

            new Thread(() -> {
                String retDepCity = DataFacade.getInstance().getCityName(returnFlight.getOrigin());
                String retArrCity = DataFacade.getInstance().getCityName(returnFlight.getDestination());
                javafx.application.Platform.runLater(() -> {
                    returnDepartureCityLabel.setText(retDepCity);
                    returnArrivalCityLabel.setText(retArrCity);
                });
            }).start();

            returnDurationLabel.setText(returnFlight.getTotalDuration());

            int returnStops = returnFlight.getNumberOfStops();
            returnStopsLabel.setText(returnStops == 0 ? "Direct" : returnStops + " stop(s)");

            returnAirlineLabel.setText("RETURN • ");


        } else {
            returnBox.setVisible(false);
            returnBox.setManaged(false);
        }
    }

    private void loadNotificationHistory() {
        if (currentTrip == null) return;

        // Limpa a lista visual
        historyListView.getItems().clear();

        // Busca os dados à BD através do Facade -> DAO
        List<TripNotification> notifications = DataFacade.getInstance().getTripHistory(currentTrip);

        if (notifications.isEmpty()) {
            // Podes adicionar um placeholder ou deixar vazio
            // historyListView.setPlaceholder(new Label("Sem alterações registadas."));
        } else {
            // Adiciona as notificações à lista
            historyListView.getItems().addAll(notifications);
        }
    }

    @FXML
    private void handleSaveAction() {
        if (currentTrip == null)
            return;

        String outboundFlightNumber = currentTrip.getOutboundFlight().getFlightNumber();
        String returnFlightNumber = currentTrip.getReturnFlight() != null
                ? currentTrip.getReturnFlight().getFlightNumber()
                : null;

        boolean isSaved = DataFacade.getInstance().isFlightSaved(outboundFlightNumber, returnFlightNumber);

        if (isSaved) {
            DataFacade.getInstance().removeSavedFlight(currentTrip);
        } else {
            DataFacade.getInstance().saveFlight(currentTrip);
        }
        updateSaveButtonState();
        DashBoardView.refreshUpcomingFlightsStatic();
    }

    private void updateSaveButtonState() {
        if (currentTrip == null || saveIcon == null)
            return;

        String outboundFlightNumber = currentTrip.getOutboundFlight().getFlightNumber();
        String returnFlightNumber = currentTrip.getReturnFlight() != null
                ? currentTrip.getReturnFlight().getFlightNumber()
                : null;

        boolean isSaved = DataFacade.getInstance().isFlightSaved(outboundFlightNumber, returnFlightNumber);

        if (isSaved) {
            saveIcon.setImage(filledHeartIcon);
            saveButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        } else {
            saveIcon.setImage(heartIcon);
            saveButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        }
    }

    @FXML
    private void handleDetailsClick() {
        if (expandedDetailsSection == null)
            return;

        isExpanded = !isExpanded;

        if (isExpanded) {
            populateExpandedDetails();
            loadNotificationHistory();

            expandedDetailsSection.setVisible(true);
            expandedDetailsSection.setManaged(true);

            if (detailsButton != null) {
                detailsButton.setText("Hide Details");
            }
        } else {
            expandedDetailsSection.setVisible(false);
            expandedDetailsSection.setManaged(false);

            if (detailsButton != null) {
                detailsButton.setText("Details");
            }
        }
    }

    private void populateExpandedDetails() {
        if (currentTrip == null)
            return;

        Flight outbound = currentTrip.getOutboundFlight();

        if (outboundFlightNumberLabel != null) {
            String flightNumber = outbound.getFlightNumber() != null ? outbound.getFlightNumber() : "N/A";
            outboundFlightNumberLabel.setText(flightNumber);
        }

        if (outboundAirlineNameLabel != null) {
            outboundAirlineNameLabel.setText(outbound.getAirlineName() != null ? outbound.getAirlineName() : "N/A");
        }

        if (outboundAircraftLabel != null) {
            outboundAircraftLabel.setText(outbound.getAircraftName());
        }

        if (outboundClassLabel != null) {
            outboundClassLabel.setText("Economy");
        }

        Flight returnFlight = currentTrip.getReturnFlight();
        if (returnFlight != null && returnDetailsBox != null) {
            returnDetailsBox.setVisible(true);
            returnDetailsBox.setManaged(true);

            if (returnFlightNumberLabel != null) {
                String flightNumber = returnFlight.getFlightNumber() != null ? returnFlight.getFlightNumber() : "N/A";
                returnFlightNumberLabel.setText(flightNumber);
            }

            if (returnAirlineNameLabel != null) {
                returnAirlineNameLabel
                        .setText(returnFlight.getAirlineName() != null ? returnFlight.getAirlineName() : "N/A");
            }

            if (returnAircraftLabel != null) {
                returnAircraftLabel.setText(returnFlight.getAircraftName());
            }

            if (returnClassLabel != null) {
                returnClassLabel.setText("Economy");
            }
        } else if (returnDetailsBox != null) {
            returnDetailsBox.setVisible(false);
            returnDetailsBox.setManaged(false);
        }
    }
}