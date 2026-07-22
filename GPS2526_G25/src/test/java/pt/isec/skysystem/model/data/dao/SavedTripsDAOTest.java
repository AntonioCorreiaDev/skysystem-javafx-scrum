package pt.isec.skysystem.model.data.dao;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pt.isec.skysystem.model.data.DataBaseManager;
import pt.isec.skysystem.model.data.flights.Trip;
import pt.isec.skysystem.model.data.flights.Flight;
import pt.isec.skysystem.model.data.flights.FlightSegment;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SavedTripsDAOTest {

    private SavedTripsDAO savedTripsDAO;
    private static final String TEST_DB_PATH = "database/skysystem_test.db";

    @BeforeEach
    void setUp() throws Exception {
        DataBaseManager.setTestMode(true);
        Files.deleteIfExists(Paths.get(TEST_DB_PATH));
        DataBaseManager.initializeDatabase();

        // Run migration to create new schema
        DataBaseManager.runMigrationIfNeeded();

        savedTripsDAO = new SavedTripsDAO();
    }

    @Test
    @DisplayName("Should save a trip successfully")
    void testSaveTrip() throws Exception {
        new UserDAO().createUser("TripUser", "trip@test.com", "hash");

        long userId = 1;

        // Create trip
        List<FlightSegment> segments = new ArrayList<>();
        segments.add(new FlightSegment("TP123", "TAP", "LIS", "OPO",
                "2025-12-25T10:00:00", "2025-12-25T11:00:00", "1H"));

        Flight outbound = new Flight("TP123", "TAP", "LIS", "OPO",
                "2025-12-25T10:00:00", "2025-12-25T11:00:00", "1H", segments);

        Trip trip = new Trip(outbound, null, 200.0);

        boolean result = savedTripsDAO.saveTrip(userId, trip);

        assertTrue(result, "Should return true when saving successfully");

        // Verify in database
        try (Connection conn = DataBaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT st.*, f.flight_number FROM SAVED_TRIPS st " +
                                "INNER JOIN FLIGHTS f ON st.outbound_flight_id = f.flight_id " +
                                "WHERE st.user_id = ?")) {

            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();

            assertTrue(rs.next(), "Record should exist in table");
            assertEquals("LIS", rs.getString("origin_iata"));
            assertEquals("200.0", rs.getString("total_price"));
            assertEquals("TP123", rs.getString("flight_number"));
        }
    }

    @Test
    @DisplayName("Should save round-trip successfully")
    void testSaveRoundTrip() throws Exception {
        new UserDAO().createUser("RoundTripUser", "roundtrip@test.com", "hash");

        long userId = 1;

        // Create outbound flight
        List<FlightSegment> outboundSegments = new ArrayList<>();
        outboundSegments.add(new FlightSegment("TP123", "TAP", "LIS", "OPO",
                "2025-12-25T10:00:00", "2025-12-25T11:00:00", "1H"));
        Flight outbound = new Flight("TP123", "TAP", "LIS", "OPO",
                "2025-12-25T10:00:00", "2025-12-25T11:00:00", "1H", outboundSegments);

        // Create return flight
        List<FlightSegment> returnSegments = new ArrayList<>();
        returnSegments.add(new FlightSegment("TP456", "TAP", "OPO", "LIS",
                "2025-12-30T14:00:00", "2025-12-30T15:00:00", "1H"));
        Flight returnFlight = new Flight("TP456", "TAP", "OPO", "LIS",
                "2025-12-30T14:00:00", "2025-12-30T15:00:00", "1H", returnSegments);

        Trip trip = new Trip(outbound, returnFlight, 400.0);

        boolean result = savedTripsDAO.saveTrip(userId, trip);

        assertTrue(result, "Should save round-trip successfully");

        // Verify both flights were saved
        try (Connection conn = DataBaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM FLIGHTS")) {
            ResultSet rs = stmt.executeQuery();
            rs.next();
            assertEquals(2, rs.getInt(1), "Should have 2 flights in database");
        }
    }

    @Test
    @DisplayName("Should retrieve saved trips correctly")
    void testGetSavedTrips() throws Exception {
        new UserDAO().createUser("GetUser", "get@test.com", "hash");
        long userId = 1;

        // Save a trip
        List<FlightSegment> segments = new ArrayList<>();
        segments.add(new FlightSegment("TP123", "TAP", "LIS", "OPO",
                "2025-12-25T10:00:00", "2025-12-25T11:00:00", "1H"));
        Flight outbound = new Flight("TP123", "TAP", "LIS", "OPO",
                "2025-12-25T10:00:00", "2025-12-25T11:00:00", "1H", segments);
        Trip trip = new Trip(outbound, null, 200.0);

        savedTripsDAO.saveTrip(userId, trip);

        // Retrieve trips
        List<Trip> retrievedTrips = savedTripsDAO.getSavedTrips(userId);

        assertEquals(1, retrievedTrips.size());
        assertEquals("TP123", retrievedTrips.get(0).getOutboundFlight().getFlightNumber());
        assertEquals(200.0, retrievedTrips.get(0).getTotalPrice(), 0.01);
    }

    @Test
    @DisplayName("DB: Should not save trip for non-existent user")
    void testSaveTripForNonExistentUser() {
        long invalidUserId = 99999;

        List<FlightSegment> segments = new ArrayList<>();
        segments.add(new FlightSegment("TP1", "TAP", "LIS", "OPO",
                "2024-01-01T10:00", "2024-01-01T11:00", "1H"));
        Flight flight = new Flight("TP1", "TAP", "LIS", "OPO",
                "2024-01-01T10:00", "2024-01-01T11:00", "1H", segments);
        Trip trip = new Trip(flight, null, 100.0);

        boolean result = savedTripsDAO.saveTrip(invalidUserId, trip);

        assertFalse(result, "Should fail when saving for non-existent user (FK violation)");
    }

    @AfterAll
    static void tearDownAll() {
        DataBaseManager.setTestMode(false);
    }
}
