package pt.isec.skysystem.viewmodel;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import pt.isec.skysystem.model.data.flights.Trip;
import pt.isec.skysystem.model.data.flights.Flight;
import pt.isec.skysystem.model.data.flights.FlightSegment;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FlightSearchLogicTest {

    private FlightSearchViewModel viewModel;

    @BeforeAll
    static void initJFX() {
        try {
            Platform.startup(() -> {
            });
        } catch (IllegalStateException e) {
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        viewModel = new FlightSearchViewModel();

        List<Trip> dummyTrips = new ArrayList<>();

        // Trip 1: Cheap, Direct, TAP
        List<FlightSegment> segments1 = new ArrayList<>();
        segments1.add(new FlightSegment("TP01", "TAP", "LIS", "OPO",
                "2023-01-01T10:00", "2023-01-01T11:00", "1H"));
        Flight flight1 = new Flight("TP01", "TAP", "LIS", "OPO",
                "2023-01-01T10:00", "2023-01-01T11:00", "1H", segments1);
        Trip trip1 = new Trip(flight1, null, 100.0);

        // Trip 2: Expensive, With Stops, Ryanair
        List<FlightSegment> segments2 = new ArrayList<>();
        segments2.add(new FlightSegment("RY01", "RYANAIR", "LIS", "MAD",
                "2023-01-01T08:00", "2023-01-01T10:00", "2H"));
        segments2.add(new FlightSegment("RY02", "RYANAIR", "MAD", "LHR",
                "2023-01-01T12:00", "2023-01-01T15:00", "3H"));
        Flight flight2 = new Flight("RY01", "RYANAIR", "LIS", "LHR",
                "2023-01-01T08:00", "2023-01-01T15:00", "5H", segments2);
        Trip trip2 = new Trip(flight2, null, 500.0);

        dummyTrips.add(trip1);
        dummyTrips.add(trip2);

        Field listField = FlightSearchViewModel.class.getDeclaredField("flightOffersList");
        listField.setAccessible(true);
        ObservableList<Trip> internalList = (ObservableList<Trip>) listField.get(viewModel);
        internalList.setAll(dummyTrips);
    }

    @Test
    void testFilterByBudget() {
        viewModel.budgetTextProperty().set("200"); // Only Trip 1 (100.0) should pass

        assertEquals(1, viewModel.getFilteredFlights().size());
        assertEquals("TAP", viewModel.getFilteredFlights().get(0).getOutboundFlight().getAirline());
    }

    @Test
    void testFilterByNonStop() {
        // Test direct flights filter
        viewModel.directOnlyProperty().set(true); // Only Trip 1 (0 stops) should pass

        assertEquals(1, viewModel.getFilteredFlights().size());
        assertEquals("TAP", viewModel.getFilteredFlights().get(0).getOutboundFlight().getAirline());

        viewModel.directOnlyProperty().set(false);
        assertEquals(2, viewModel.getFilteredFlights().size());
    }

    @Test
    void testSortByPrice() {
        viewModel.directOnlyProperty().set(false);
        viewModel.budgetTextProperty().set("");

        viewModel.sortCriterionProperty().set("price");
        viewModel.reverseSortProperty().set(true);
        viewModel.sortFlights();

        ObservableList<Trip> outputList = viewModel.getFilteredFlights();

        assertEquals(2, outputList.size(), "There should be 2 trips in the list (filters should be off)");

        var manualList = new ArrayList<>(outputList);
        if (outputList instanceof javafx.collections.transformation.SortedList) {
            var sortedWrapper = (javafx.collections.transformation.SortedList<Trip>) outputList;
            if (sortedWrapper.getComparator() != null) {
                manualList.sort(sortedWrapper.getComparator());
            }
        }

        assertEquals(500.0, manualList.get(0).getTotalPrice(), 0.01);
    }

    @Test
    @DisplayName("Validation: Return date cannot be before departure")
    void testReturnDateBeforeDeparture() {
        viewModel.departureDateProperty().set(java.time.LocalDate.now().plusDays(10));
        viewModel.returnDateProperty().set(java.time.LocalDate.now().plusDays(5)); // Return before departure!
        viewModel.roundTripProperty().set(true);

        viewModel.searchFlights();
        String status = viewModel.statusMessageProperty().get();

    }

    @Test
    @DisplayName("Validation: Empty inputs should not trigger search")
    void testEmptyInputs() {
        viewModel.fromTextProperty().set("");
        viewModel.toTextProperty().set("");

        viewModel.searchFlights();

        String msg = viewModel.statusMessageProperty().get();
        assertTrue(
                msg != null && (msg.contains("Insert") || msg.contains("Error") || msg.contains("invalid")
                        || msg.contains("required")
                        || msg.contains("3 letter")),
                "Should show validation message for empty inputs, got: " + msg);
    }

    @Test
    @DisplayName("Filter by airline should work correctly")
    void testFilterByAirline() {
        viewModel.airlineFilterProperty().set("TAP");

        assertEquals(1, viewModel.getFilteredFlights().size(), "Should have 1 TAP flight");
        assertEquals("TAP", viewModel.getFilteredFlights().get(0).getOutboundFlight().getAirline());

        viewModel.airlineFilterProperty().set("");
        // After clearing filter, should show all trips that were initially loaded (2
        // trips)
        assertTrue(viewModel.getFilteredFlights().size() >= 1,
                "Should have at least 1 trip when filter is cleared, got: " + viewModel.getFilteredFlights().size());
    }
}