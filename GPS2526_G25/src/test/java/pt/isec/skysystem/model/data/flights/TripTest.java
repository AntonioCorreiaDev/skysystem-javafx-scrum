package pt.isec.skysystem.model.data.flights;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class TripTest {

    @Test
    @DisplayName("Should correctly identify one-way trips")
    void testOneWayTrip() {
        // Create flight segments
        List<FlightSegment> segments = new ArrayList<>();
        segments.add(new FlightSegment("TP1", "TAP", "LIS", "OPO",
                "2024-01-01T10:00", "2024-01-01T11:00", "1H"));

        // Create outbound flight
        Flight outbound = new Flight("TP123", "TAP", "LIS", "OPO",
                "2024-01-01T10:00", "2024-01-01T11:00", "1H", segments);

        // Create one-way trip
        Trip trip = new Trip(outbound, null, 100.0);

        assertFalse(trip.isRoundTrip());
        assertNull(trip.getReturnFlight());
        assertEquals(100.0, trip.getTotalPrice(), 0.01);
    }

    @Test
    @DisplayName("Should correctly identify round-trip flights")
    void testRoundTrip() {
        // Create outbound flight
        List<FlightSegment> outboundSegments = new ArrayList<>();
        outboundSegments.add(new FlightSegment("TP1", "TAP", "LIS", "OPO",
                "2024-01-01T10:00", "2024-01-01T11:00", "1H"));
        Flight outbound = new Flight("TP123", "TAP", "LIS", "OPO",
                "2024-01-01T10:00", "2024-01-01T11:00", "1H", outboundSegments);

        // Create return flight
        List<FlightSegment> returnSegments = new ArrayList<>();
        returnSegments.add(new FlightSegment("TP2", "TAP", "OPO", "LIS",
                "2024-01-05T14:00", "2024-01-05T15:00", "1H"));
        Flight returnFlight = new Flight("TP456", "TAP", "OPO", "LIS",
                "2024-01-05T14:00", "2024-01-05T15:00", "1H", returnSegments);

        // Create round-trip
        Trip trip = new Trip(outbound, returnFlight, 200.0);

        assertTrue(trip.isRoundTrip());
        assertNotNull(trip.getReturnFlight());
        assertEquals(200.0, trip.getTotalPrice(), 0.01);
        assertEquals("TP456", trip.getReturnFlight().getFlightNumber());
    }

    @Test
    @DisplayName("Should calculate number of stops correctly")
    void testStopCount() {
        // Direct flight (1 segment = 0 stops)
        List<FlightSegment> directSegments = new ArrayList<>();
        directSegments.add(new FlightSegment("TP1", "TAP", "LIS", "OPO",
                "2024-01-01T10:00", "2024-01-01T11:00", "1H"));
        Flight directFlight = new Flight("TP123", "TAP", "LIS", "OPO",
                "2024-01-01T10:00", "2024-01-01T11:00", "1H", directSegments);

        assertEquals(0, directFlight.getNumberOfStops());

        // Flight with 1 stop (2 segments)
        List<FlightSegment> oneStopSegments = new ArrayList<>();
        oneStopSegments.add(new FlightSegment("TP1", "TAP", "LIS", "MAD",
                "2024-01-01T10:00", "2024-01-01T12:00", "2H"));
        oneStopSegments.add(new FlightSegment("TP2", "TAP", "MAD", "OPO",
                "2024-01-01T14:00", "2024-01-01T16:00", "2H"));
        Flight oneStopFlight = new Flight("TP456", "TAP", "LIS", "OPO",
                "2024-01-01T10:00", "2024-01-01T16:00", "6H", oneStopSegments);

        assertEquals(1, oneStopFlight.getNumberOfStops());
    }

    @Test
    @DisplayName("Should handle flight details correctly")
    void testFlightDetails() {
        List<FlightSegment> segments = new ArrayList<>();
        segments.add(new FlightSegment("TP1", "TAP", "LIS", "OPO",
                "2024-01-01T10:00", "2024-01-01T11:00", "1H"));

        Flight flight = new Flight("TP123", "TAP", "LIS", "OPO",
                "2024-01-01T10:00", "2024-01-01T11:00", "1H", segments);

        assertEquals("TP123", flight.getFlightNumber());
        assertEquals("TAP", flight.getAirline());
        assertEquals("LIS", flight.getOrigin());
        assertEquals("OPO", flight.getDestination());
        assertEquals("2024-01-01T10:00", flight.getDepartureTime());
        assertEquals("2024-01-01T11:00", flight.getArrivalTime());
        assertEquals("1H", flight.getTotalDuration());
        assertEquals(1, flight.getSegments().size());
    }

    @Test
    @DisplayName("Should handle flight segment details correctly")
    void testFlightSegmentDetails() {
        FlightSegment segment = new FlightSegment("TP1", "TAP", "LIS", "OPO",
                "2024-01-01T10:00", "2024-01-01T11:00", "1H");

        assertEquals("TP1", segment.getFlightNumber());
        assertEquals("TAP", segment.getCarrierCode());
        assertEquals("LIS", segment.getOrigin());
        assertEquals("OPO", segment.getDestination());
        assertEquals("2024-01-01T10:00", segment.getDepartureTime());
        assertEquals("2024-01-01T11:00", segment.getArrivalTime());
        assertEquals("1H", segment.getDuration());
    }

    @Test
    @DisplayName("Should generate unique trip IDs")
    void testTripIdGeneration() {
        List<FlightSegment> segments1 = new ArrayList<>();
        segments1.add(new FlightSegment("TP1", "TAP", "LIS", "OPO",
                "2024-01-01T10:00", "2024-01-01T11:00", "1H"));
        Flight outbound1 = new Flight("TP123", "TAP", "LIS", "OPO",
                "2024-01-01T10:00", "2024-01-01T11:00", "1H", segments1);

        List<FlightSegment> segments2 = new ArrayList<>();
        segments2.add(new FlightSegment("TP2", "TAP", "LIS", "OPO",
                "2024-01-02T10:00", "2024-01-02T11:00", "1H"));
        Flight outbound2 = new Flight("TP456", "TAP", "LIS", "OPO",
                "2024-01-02T10:00", "2024-01-02T11:00", "1H", segments2);

        Trip trip1 = new Trip(outbound1, null, 100.0);
        Trip trip2 = new Trip(outbound2, null, 100.0);

        assertNotNull(trip1.getId());
        assertNotNull(trip2.getId());
        assertNotEquals(trip1.getId(), trip2.getId(), "Trip IDs should be different for different flights");
    }
}
