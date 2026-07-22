package pt.isec.skysystem.model.data;

import org.junit.jupiter.api.Test;
import pt.isec.skysystem.model.data.flights.Trip;
import pt.isec.skysystem.model.data.flights.Flight;
import pt.isec.skysystem.model.data.flights.FlightSegment;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserProfileTest {

        @Test
        void testAddSavedFlight() {
                UserProfile user = new UserProfile("testuser", "test@example.com", true);

                // Create two Trip objects with SAME flight numbers (should be considered equal)
                List<FlightSegment> segments = new ArrayList<>();
                segments.add(new FlightSegment("TP1", "TAP", "LIS", "OPO",
                                "2024-01-01T10:00", "2024-01-01T11:00", "1H"));

                Flight flight1 = new Flight("TP123", "TAP", "LIS", "OPO",
                                "2024-01-01T10:00", "2024-01-01T11:00", "1H", segments);
                Trip trip1 = new Trip(flight1, null, 100.0);

                Flight flight2 = new Flight("TP123", "TAP", "LIS", "OPO",
                                "2024-01-01T10:00", "2024-01-01T11:00", "1H", segments);
                Trip trip2 = new Trip(flight2, null, 100.0);

                user.addSavedFlight(trip1);
                user.addSavedFlight(trip2); // Should not add duplicate

                assertEquals(1, user.getSavedFlights().size());
        }

        @Test
        void testRemoveSavedFlight() {
                UserProfile user = new UserProfile("testuser", "test@example.com", true);

                List<FlightSegment> segments = new ArrayList<>();
                segments.add(new FlightSegment("TP1", "TAP", "LIS", "OPO",
                                "2024-01-01T10:00", "2024-01-01T11:00", "1H"));
                Flight flight = new Flight("TP123", "TAP", "LIS", "OPO",
                                "2024-01-01T10:00", "2024-01-01T11:00", "1H", segments);
                Trip trip = new Trip(flight, null, 100.0);

                user.addSavedFlight(trip);
                assertEquals(1, user.getSavedFlights().size());

                // Create trip with same flight number to remove
                Flight flightToRemove = new Flight("TP123", "TAP", "LIS", "OPO",
                                "2024-01-01T10:00", "2024-01-01T11:00", "1H", segments);
                Trip tripToRemove = new Trip(flightToRemove, null, 999.0); // Different price but same flight number

                user.removeSavedFlight(tripToRemove);
                assertEquals(0, user.getSavedFlights().size());
        }

        @Test
        void testUserProfileBasicFields() {
                UserProfile user = new UserProfile("testuser", "test@example.com", true);

                assertEquals("testuser", user.getUsername());
                assertEquals("test@example.com", user.getEmail());
                assertNotNull(user.getSavedFlights());
                assertTrue(user.getSavedFlights().isEmpty());
        }

        @Test
        void testSavedFlightsInitialization() {
                UserProfile user = new UserProfile("testuser", "test@example.com", true);

                assertNotNull(user.getSavedFlights());
                assertEquals(0, user.getSavedFlights().size());
        }
}