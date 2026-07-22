package pt.isec.skysystem.viewmodel;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import pt.isec.skysystem.model.DataFacade;
import pt.isec.skysystem.model.data.flights.Trip;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class FlightSearchViewModel {

    private static final Map<String, String> CITY_TO_IATA;

    public String getLastPage() {
        return dataFacade.getLastPage();
    }

    static {
        Map<String, String> map = new HashMap<>();

        // --- Portugal ---
        map.put("LISBOA", "LIS");
        map.put("LIS", "LIS");
        map.put("PORTO", "OPO");
        map.put("OPORTO", "OPO");
        map.put("OPO", "OPO");
        map.put("FARO", "FAO");
        map.put("ALGARVE", "FAO");
        map.put("FAO", "FAO");
        map.put("FUNCHAL", "FNC");
        map.put("MADEIRA", "FNC");
        map.put("FNC", "FNC");
        map.put("PONTA DELGADA", "PDL");
        map.put("AÇORES", "PDL");
        map.put("PDL", "PDL");
        map.put("LAJES", "TER");
        map.put("TER", "TER");
        map.put("BEJA", "BYJ");
        map.put("BYJ", "BYJ");

        // --- Europa ---
        map.put("MADRID", "MAD");
        map.put("MAD", "MAD");
        map.put("BARCELONA", "BCN");
        map.put("BCN", "BCN");
        map.put("PARIS", "CDG");
        map.put("CDG", "CDG");
        map.put("ORLY", "ORY");
        map.put("ORY", "ORY");
        map.put("LONDRES", "LHR");
        map.put("HEATHROW", "LHR");
        map.put("LHR", "LHR");
        map.put("GATWICK", "LGW");
        map.put("LGW", "LGW");
        map.put("FRANKFURT", "FRA");
        map.put("FRA", "FRA");
        map.put("AMSTERDAM", "AMS");
        map.put("SCHIPHOL", "AMS");
        map.put("AMS", "AMS");
        map.put("ROMA", "FCO");
        map.put("FIUMICINO", "FCO");
        map.put("FCO", "FCO");
        map.put("MILÃO", "MXP");
        map.put("MXP", "MXP");
        map.put("BRUXELAS", "BRU");
        map.put("BRU", "BRU");
        map.put("DUBLIN", "DUB");
        map.put("DUB", "DUB");
        map.put("VIENA", "VIE");
        map.put("VIE", "VIE");
        map.put("OSLO", "OSL");
        map.put("OSL", "OSL");
        map.put("BERLIM", "TXL");
        map.put("TXL", "TXL");
        map.put("PRAGA", "PRG");
        map.put("PRG", "PRG");

        // --- Destinos Internacionais ---
        map.put("NOVA YORK", "JFK");
        map.put("NYC", "JFK");
        map.put("JFK", "JFK");
        map.put("SÃO PAULO", "GRU");
        map.put("GUARULHOS", "GRU");
        map.put("GRU", "GRU");
        map.put("RIO DE JANEIRO", "GIG");
        map.put("GIG", "GIG");
        map.put("DUBAI", "DXB");
        map.put("DXB", "DXB");
        map.put("HONG KONG", "HKG");
        map.put("HKG", "HKG");

        CITY_TO_IATA = Collections.unmodifiableMap(map);
    }

    // --- Propriedades de Input ---
    private final StringProperty fromText = new SimpleStringProperty("");
    private final StringProperty toText = new SimpleStringProperty("");
    private final ObjectProperty<LocalDate> departureDate = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> returnDate = new SimpleObjectProperty<>();
    private final StringProperty budgetText = new SimpleStringProperty("");

    // --- Propriedades de Opções ---
    private final BooleanProperty roundTrip = new SimpleBooleanProperty(true);
    private final BooleanProperty directOnly = new SimpleBooleanProperty(true);
    private final IntegerProperty nPassengers = new SimpleIntegerProperty(1);
    private final ObservableList<Integer> availablePassengers = FXCollections.observableArrayList(1, 2, 3, 4, 5, 6, 7,
            8, 9);

    private final StringProperty airlineFilter = new SimpleStringProperty("");
    private final BooleanProperty reverseSort = new SimpleBooleanProperty(false); // true = DESC, false = ASC

    // --- Propriedades de Sorting ---
    private final ObjectProperty<String> sortCriterion = new SimpleObjectProperty<>("price");
    private final SortedList<Trip> sortedFlights;

    // --- Propriedades de Output e Estado ---
    private final ObservableList<Trip> flightOffersList = FXCollections.observableArrayList();
    private final FilteredList<Trip> filteredFlights = new FilteredList<>(flightOffersList, p -> true);
    private final BooleanProperty searchInProgress = new SimpleBooleanProperty(false);
    private final StringProperty statusMessage = new SimpleStringProperty("Insert your flight search.");
    private final BooleanProperty noResults = new SimpleBooleanProperty(false);
    private final BooleanProperty hasSearched = new SimpleBooleanProperty(false);

    // --- Propriedades de Validação ---
    private final StringProperty fromError = new SimpleStringProperty("");
    private final StringProperty toError = new SimpleStringProperty("");
    private final StringProperty departureDateError = new SimpleStringProperty("");
    private final StringProperty returnDateError = new SimpleStringProperty("");

    private final DataFacade dataFacade;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public FlightSearchViewModel() {
        this.dataFacade = DataFacade.getInstance();

        if (dataFacade.getFlightToSearch() != null) {
            setToFlight(dataFacade.getFlightToSearch());
            dataFacade.setFlightToSearch(null);
        }

        this.sortedFlights = new SortedList<>(filteredFlights);

        sortedFlights.addListener((javafx.collections.ListChangeListener<Trip>) c -> {
            noResults.set(sortedFlights.isEmpty() && hasSearched.get());
        });

        // Listeners para atualizar filtros automaticamente
        budgetText.addListener((obs, oldVal, newVal) -> applyFilters());
        directOnly.addListener((obs, oldVal, newVal) -> applyFilters());
        airlineFilter.addListener((obs, oldVal, newVal) -> applyFilters());

        sortCriterion.addListener((obs, oldVal, newVal) -> sortFlights());
        reverseSort.addListener((obs, oldVal, newVal) -> sortFlights());

        applyFilters();
    }

    public void setToFlight(String destination) {
        toText.set(destination);
    }

    public void searchFlights() {
        fromError.set("");
        toError.set("");
        departureDateError.set("");
        returnDateError.set("");

        // Validate inputs
        boolean hasErrors = false;

        String origin = getIataCode(fromText.get());
        if (origin == null || origin.isEmpty()) {
            fromError.set("Invalid origin");
            hasErrors = true;
        }

        String destination = getIataCode(toText.get());
        if (destination == null || destination.isEmpty()) {
            toError.set("Invalid destination");
            hasErrors = true;
        }

        if (departureDate.get() == null) {
            departureDateError.set("Departure date required");
            hasErrors = true;
        }

        String depDateStr = departureDate.get() != null ? departureDate.get().format(DATE_FORMAT) : null;
        String retDateStr = null;
        if (roundTrip.get() && returnDate.get() != null) {
            retDateStr = returnDate.get().format(DATE_FORMAT);
        } else if (roundTrip.get()) {
            returnDateError.set("Return date required");
            hasErrors = true;
        }

        if (hasErrors) {
            return;
        }

        int passengers = nPassengers.get();
        boolean isDirect = directOnly.get();

        String finalRetDateStr = retDateStr;
        String finalOrigin = origin;
        String finalDestination = destination;

        javafx.concurrent.Task<List<Trip>> searchTask = new javafx.concurrent.Task<>() {
            @Override
            protected List<Trip> call() {
                String originIATA = getIataCode(finalOrigin);
                String destIATA = getIataCode(finalDestination);
                return dataFacade.searchFlights(originIATA, destIATA, depDateStr, finalRetDateStr, passengers,
                        isDirect);
            }

            @Override
            protected void succeeded() {
                List<Trip> results = getValue();
                flightOffersList.setAll(results);
                applyFilters();
                sortFlights();
                hasSearched.set(true);
                noResults.set(filteredFlights.isEmpty());
                statusMessage.set(flightOffersList.isEmpty() ? "No flights found."
                        : flightOffersList.size() + " flights found. Applying filters...");
            }

            @Override
            protected void failed() {
                hasSearched.set(true);
                noResults.set(true);
                statusMessage.set("Error in search: " + getException().getMessage());
                getException().printStackTrace();
            }

            @Override
            protected void running() {
                statusMessage.set("Searching...");
                searchInProgress.set(true);
            }

            @Override
            protected void done() {
                searchInProgress.set(false);
            }
        };

        new Thread(searchTask).start();
    }

    /**
     * Aplica os filtros de Preço, Voos Diretos e Companhia Aérea na FilteredList.
     */
    private void applyFilters() {
        double budgetLimit = 0.0;
        try {
            String budgetTextValue = budgetText.get().trim().replace(',', '.');
            if (!budgetTextValue.isEmpty()) {
                budgetLimit = Double.parseDouble(budgetTextValue);
            }
        } catch (NumberFormatException e) {
            budgetLimit = 0.0;
        }
        final double finalBudget = budgetLimit;
        final boolean isDirectOnly = directOnly.get();
        final String selectedAirline = airlineFilter.get().trim().toUpperCase();

        filteredFlights.setPredicate(trip -> {

            if (finalBudget > 0 && trip.getTotalPrice() > finalBudget) {
                return false;
            }

            if (isDirectOnly && trip.getOutboundFlight().getNumberOfStops() > 0) {
                return false;
            }

            if (!selectedAirline.isEmpty()) {

                boolean matchOutbound = trip.getOutboundFlight().getAirline() != null &&
                        trip.getOutboundFlight().getAirline().trim().toUpperCase().contains(selectedAirline);

                boolean matchReturn = trip.isRoundTrip() &&
                        trip.getReturnFlight() != null &&
                        trip.getReturnFlight().getAirline() != null &&
                        trip.getReturnFlight().getAirline().trim().toUpperCase().contains(selectedAirline);

                if (!matchOutbound && !matchReturn) {
                    return false;
                }
            }

            return true;
        });

        noResults.set(filteredFlights.isEmpty() && hasSearched.get());

        if (!searchInProgress.get()) {
            statusMessage.set(filteredFlights.isEmpty() && flightOffersList.isEmpty() ? "Insert your flight search."
                    : filteredFlights.isEmpty() ? "No flights found."
                            : filteredFlights.size() + " flights found.");
        }
    }

    private int convertDurationStringToMinutes(String duration) {
        if (duration == null || duration.isEmpty())
            return Integer.MAX_VALUE;

        int totalMinutes = 0;
        String current = duration;

        if (current.contains("D")) {
            try {
                int days = Integer.parseInt(current.substring(0, current.indexOf('D')));
                totalMinutes += days * 24 * 60;
                current = current.substring(current.indexOf('D') + 1);
            } catch (NumberFormatException ignored) {
            }
        }

        if (current.contains("H")) {
            try {
                int hours = Integer.parseInt(current.substring(0, current.indexOf('H')));
                totalMinutes += hours * 60;
                current = current.substring(current.indexOf('H') + 1);
            } catch (NumberFormatException ignored) {
            }
        }

        if (current.contains("M")) {
            try {
                int minutes = Integer.parseInt(current.substring(0, current.indexOf('M')));
                totalMinutes += minutes;
            } catch (NumberFormatException ignored) {
            }
        }

        return totalMinutes == 0 ? Integer.MAX_VALUE : totalMinutes;
    }

    /**
     * Obtém a duração total da viagem (incluindo escalas) em minutos.
     */
    private int getTripTotalDurationMinutes(Trip trip) {
        int total = convertDurationStringToMinutes(trip.getOutboundFlight().getTotalDuration());
        if (trip.isRoundTrip()) {
            total += convertDurationStringToMinutes(trip.getReturnFlight().getTotalDuration());
        }
        return total;
    }

    /**
     * Obtém o tempo total de voo (tempo no ar, excluindo escalas) em minutos.
     */
    private int getTripTotalFlightTimeMinutes(Trip trip) {
        return getTripTotalDurationMinutes(trip);
    }

    /**
     * Define o Comparator para a SortedList, incluindo a lógica de inversão
     * (ASC/DESC).
     */
    public void sortFlights() {
        Comparator<Trip> comparator = switch (sortCriterion.get()) {
            case "duration" -> Comparator.comparingInt(this::getTripTotalDurationMinutes);
            case "flight_time" -> Comparator.comparingInt(this::getTripTotalFlightTimeMinutes);
            case "airline" ->
                Comparator.comparing(trip -> trip.getOutboundFlight().getAirline(), String.CASE_INSENSITIVE_ORDER);
            case "price" -> Comparator.comparingDouble(Trip::getTotalPrice);
            default -> Comparator.comparingDouble(Trip::getTotalPrice);
        };

        if (reverseSort.get() &&
                (sortCriterion.get().equals("price") ||
                        sortCriterion.get().equals("duration") ||
                        sortCriterion.get().equals("flight_time"))) {

            comparator = comparator.reversed();
        }

        sortedFlights.setComparator(comparator);
    }

    public String getIataCode(String cityOrIata) {
        if (cityOrIata == null || cityOrIata.trim().isEmpty()) {
            return "";
        }
        String normalizedInput = cityOrIata.trim().toUpperCase(Locale.ROOT);

        if (CITY_TO_IATA.containsKey(normalizedInput)) {
            return CITY_TO_IATA.get(normalizedInput);
        }

        if (normalizedInput.length() == 3 && normalizedInput.matches("[A-Z]{3}")) {
            return normalizedInput;
        }

        try {
            String iataCode = dataFacade.searchLocation(cityOrIata);
            if (iataCode != null && !iataCode.isEmpty()) {
                return iataCode;
            }
        } catch (Exception e) {
            System.err.println("Error searching location via Amadeus: " + e.getMessage());
        }

        return normalizedInput;
    }

    public void resetFields() {
        fromText.set("");
        toText.set("");
        departureDate.set(null);
        returnDate.set(null);
        budgetText.set("");
        directOnly.set(true);
        airlineFilter.set("");
        reverseSort.set(false);
        nPassengers.set(1);
        hasSearched.set(false);
        fromError.set("");
        toError.set("");
        departureDateError.set("");
        returnDateError.set("");
        flightOffersList.clear();
        statusMessage.set("Fields cleared. Ready for new search.");
    }

    // --- GETTERS ---
    public StringProperty fromTextProperty() {
        return fromText;
    }

    public StringProperty toTextProperty() {
        return toText;
    }

    public ObjectProperty<LocalDate> departureDateProperty() {
        return departureDate;
    }

    public ObjectProperty<LocalDate> returnDateProperty() {
        return returnDate;
    }

    public StringProperty budgetTextProperty() {
        return budgetText;
    }

    public BooleanProperty roundTripProperty() {
        return roundTrip;
    }

    public BooleanProperty directOnlyProperty() {
        return directOnly;
    }

    public IntegerProperty nPassengersProperty() {
        return nPassengers;
    }

    public ObservableList<Integer> getAvailablePassengers() {
        return availablePassengers;
    }

    public StringProperty airlineFilterProperty() {
        return airlineFilter;
    }

    public BooleanProperty reverseSortProperty() {
        return reverseSort;
    }

    public ReadOnlyStringProperty statusMessageProperty() {
        return statusMessage;
    }

    public ReadOnlyBooleanProperty searchInProgressProperty() {
        return searchInProgress;
    }

    public ReadOnlyBooleanProperty noResultsProperty() {
        return noResults;
    }

    public BooleanProperty hasSearchedProperty() {
        return hasSearched;
    }

    public StringProperty fromErrorProperty() {
        return fromError;
    }

    public StringProperty toErrorProperty() {
        return toError;
    }

    public StringProperty departureDateErrorProperty() {
        return departureDateError;
    }

    public StringProperty returnDateErrorProperty() {
        return returnDateError;
    }

    public ObservableList<Trip> getFilteredFlights() {
        return sortedFlights;
    }

    // Getter para ligar a ComboBox ao ViewModel
    public ObjectProperty<String> sortCriterionProperty() {
        return sortCriterion;
    }
}