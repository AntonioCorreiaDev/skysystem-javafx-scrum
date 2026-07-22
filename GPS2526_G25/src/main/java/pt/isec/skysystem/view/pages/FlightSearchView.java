package pt.isec.skysystem.view.pages;

import pt.isec.skysystem.SkySystemApp;
import pt.isec.skysystem.model.data.flights.Trip;
import pt.isec.skysystem.viewmodel.FlightSearchViewModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import java.io.IOException;
import java.time.LocalDate;

/**
 * Lida apenas com a interação do utilizador e faz BINDING com o ViewModel.
 */
public class FlightSearchView {

    // --- Componentes FXML ---
    @FXML
    private TextField fromLabel;
    @FXML
    private TextField ToLabel;
    @FXML
    private DatePicker DepartureDate;
    @FXML
    private DatePicker ReturnDate;
    @FXML
    private TextField BudgetLabel;
    @FXML
    private Button SearchButton;
    @FXML
    private Button ResetButton;
    @FXML
    private ListView<Trip> flightOffersList;
    @FXML
    private Label statusLabel;
    @FXML
    private Label noResultsLabel;
    @FXML
    private VBox loadingContainer;
    @FXML
    private ProgressIndicator loadingIndicator;
    @FXML
    private Label loadingLabel;
    @FXML
    private Label topFlightOffersLabel;
    @FXML
    private Label fromErrorLabel;
    @FXML
    private Label toErrorLabel;
    @FXML
    private Label departureDateErrorLabel;
    @FXML
    private Label returnDateErrorLabel;
    @FXML
    private ComboBox<String> sortCriterionComboBox;

    // Campos de filtro e opções
    @FXML
    private RadioButton roundTripRadio;
    @FXML
    private RadioButton oneWayRadio;
    @FXML
    private CheckBox isDirectCheckBox;
    @FXML
    private Spinner<Integer> passengersSpinner;
    @FXML
    private ToggleGroup tripTypeGroup;
    @FXML
    private TextField airlineFilterField;
    @FXML
    private CheckBox reverseSortCheckBox;

    private FlightSearchViewModel viewModel;

    @FXML
    public void initialize() {
        viewModel = new FlightSearchViewModel();
        setupBindings();
        setupValidationLogic();
        setupResultList();
    }

    /**
     * Configura todos os BINDINGS (Bidirecionais e Unidirecionais) com o ViewModel.
     */
    private void setupBindings() {

        // --- Bindings Bidirecionais (Input) ---
        fromLabel.textProperty().bindBidirectional(viewModel.fromTextProperty());
        ToLabel.textProperty().bindBidirectional(viewModel.toTextProperty());
        DepartureDate.valueProperty().bindBidirectional(viewModel.departureDateProperty());
        ReturnDate.valueProperty().bindBidirectional(viewModel.returnDateProperty());
        BudgetLabel.textProperty().bindBidirectional(viewModel.budgetTextProperty());
        isDirectCheckBox.selectedProperty().bindBidirectional(viewModel.directOnlyProperty());

        viewModel.roundTripProperty().bind(roundTripRadio.selectedProperty());

        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 9, 1);
        passengersSpinner.setValueFactory(valueFactory);
        passengersSpinner.getValueFactory().valueProperty()
                .bindBidirectional(viewModel.nPassengersProperty().asObject());

        airlineFilterField.textProperty().bindBidirectional(viewModel.airlineFilterProperty());

        // --- Bindings de Ordenação ---
        sortCriterionComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                switch (newVal) {
                    case "Price Ascending" -> {
                        viewModel.sortCriterionProperty().set("price");
                        viewModel.reverseSortProperty().set(false);
                    }
                    case "Price Descending" -> {
                        viewModel.sortCriterionProperty().set("price");
                        viewModel.reverseSortProperty().set(true);
                    }
                    case "Duration" -> {
                        viewModel.sortCriterionProperty().set("duration");
                        viewModel.reverseSortProperty().set(false);
                    }
                    case "Flight Time" -> {
                        viewModel.sortCriterionProperty().set("flight_time");
                        viewModel.reverseSortProperty().set(false);
                    }
                }
            }
        });

        // --- Bindings Unidirecionais (Output e Estado) ---
        flightOffersList.setItems(viewModel.getFilteredFlights());

        if (statusLabel != null) {
            statusLabel.textProperty().bind(viewModel.statusMessageProperty());
        }
        SearchButton.disableProperty().bind(viewModel.searchInProgressProperty());

        if (loadingContainer != null) {
            loadingContainer.visibleProperty().bind(viewModel.searchInProgressProperty());
            loadingContainer.managedProperty().bind(viewModel.searchInProgressProperty());
        }

        if (fromErrorLabel != null) {
            fromErrorLabel.textProperty().bind(viewModel.fromErrorProperty());
            fromErrorLabel.visibleProperty().bind(viewModel.fromErrorProperty().isNotEmpty());
            fromErrorLabel.managedProperty().bind(viewModel.fromErrorProperty().isNotEmpty());
        }
        if (toErrorLabel != null) {
            toErrorLabel.textProperty().bind(viewModel.toErrorProperty());
            toErrorLabel.visibleProperty().bind(viewModel.toErrorProperty().isNotEmpty());
            toErrorLabel.managedProperty().bind(viewModel.toErrorProperty().isNotEmpty());
        }
        if (departureDateErrorLabel != null) {
            departureDateErrorLabel.textProperty().bind(viewModel.departureDateErrorProperty());
            departureDateErrorLabel.visibleProperty().bind(viewModel.departureDateErrorProperty().isNotEmpty());
            departureDateErrorLabel.managedProperty().bind(viewModel.departureDateErrorProperty().isNotEmpty());
        }
        if (returnDateErrorLabel != null) {
            returnDateErrorLabel.textProperty().bind(viewModel.returnDateErrorProperty());
            returnDateErrorLabel.visibleProperty().bind(viewModel.returnDateErrorProperty().isNotEmpty());
            returnDateErrorLabel.managedProperty().bind(viewModel.returnDateErrorProperty().isNotEmpty());
        }

        if (topFlightOffersLabel != null) {
            topFlightOffersLabel.visibleProperty().bind(viewModel.hasSearchedProperty());
            topFlightOffersLabel.managedProperty().bind(viewModel.hasSearchedProperty());
        }

        if (noResultsLabel != null) {
            flightOffersList.visibleProperty()
                    .bind(viewModel.noResultsProperty().not().and(viewModel.hasSearchedProperty()));
            flightOffersList.managedProperty()
                    .bind(viewModel.noResultsProperty().not().and(viewModel.hasSearchedProperty()));
            noResultsLabel.visibleProperty().bind(viewModel.noResultsProperty().and(viewModel.hasSearchedProperty()));
            noResultsLabel.managedProperty().bind(viewModel.noResultsProperty().and(viewModel.hasSearchedProperty()));
        }
    }

    private void setupValidationLogic() {
        ReturnDate.disableProperty().bind(oneWayRadio.selectedProperty());

        if (DepartureDate != null) {
            DepartureDate.setDayCellFactory(getDepartureDayCellFactory());
        }

        if (DepartureDate != null && ReturnDate != null) {
            DepartureDate.valueProperty().addListener((obs, oldDate, newDate) -> {
                ReturnDate.setDayCellFactory(getReturnDayCellFactory(newDate));
            });
            ReturnDate.setDayCellFactory(getReturnDayCellFactory(DepartureDate.getValue()));
        }
    }

    private void setupResultList() {
        flightOffersList.setCellFactory(flightListView -> new FlightOfferListCell());
        flightOffersList.setStyle("-fx-background-color: transparent;");
    }

    // Factory para garantir que a data de partida não é passada.
    private Callback<DatePicker, DateCell> getDepartureDayCellFactory() {
        return datePicker -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (item.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #ffc0cb;");
                }
            }
        };
    }

    // Factory para garantir que a data de regresso é posterior à data de partida.
    private Callback<DatePicker, DateCell> getReturnDayCellFactory(LocalDate departureDateRef) {
        return datePicker -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);

                LocalDate minDate = departureDateRef != null ? departureDateRef.plusDays(1) : LocalDate.now();

                if (item.isBefore(minDate)) {
                    setDisable(true);
                }

                if (isDisabled()) {
                    setStyle("-fx-background-color: #ffc0cb;");
                }
            }
        };
    }

    @FXML
    public void handleSearchButtonAction() {
        viewModel.searchFlights();
    }

    @FXML
    public void handleResetButtonAction() {
        viewModel.resetFields();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void navigateToLastPage() {
        if (SearchButton == null || SearchButton.getScene() == null) {
            showError("The interface is not fully initialized.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/" + viewModel.getLastPage() + ".fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) SearchButton.getScene().getWindow();
            Scene scene = new Scene(root, SkySystemApp.WINDOW_WIDTH, SkySystemApp.WINDOW_HEIGHT);
            stage.setScene(scene);
            stage.setTitle(SkySystemApp.APP_TITLE);

            System.out.println("Navigated to " + viewModel.getLastPage());

        } catch (IOException e) {
            System.err.println("Failed to navigate to " + viewModel.getLastPage() + ": " + e.getMessage());
        }
    }

    @FXML
    public void handleBackButton(ActionEvent actionEvent) {
        navigateToLastPage();
    }
}