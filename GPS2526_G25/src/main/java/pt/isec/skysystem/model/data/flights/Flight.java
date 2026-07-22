package pt.isec.skysystem.model.data.flights;

import java.util.List;

/**
 * Represents a single directional flight
 * A flight may consist of one or more segments if there are layovers.
 */
public class Flight {

    private final String airline;
    private final String origin;
    private final String destination;
    private final String departureTime;
    private final String arrivalTime;
    private final String totalDuration;
    private final String flightNumber;
    private final List<FlightSegment> segments;
    private String airlineName;
    private String aircraftName;

    /**
     * Creates a new Flight from a list of segments.
     *
     * @param segments List of flight segments
     */
    public Flight(List<FlightSegment> segments) {
        if (segments == null || segments.isEmpty()) {
            throw new IllegalArgumentException("Flight must have at least one segment");
        }

        this.segments = segments;

        FlightSegment firstSegment = segments.get(0);
        FlightSegment lastSegment = segments.get(segments.size() - 1);

        this.airline = firstSegment.getCarrierCode();
        this.origin = firstSegment.getOrigin();
        this.destination = lastSegment.getDestination();
        this.departureTime = firstSegment.getDepartureTime();
        this.arrivalTime = lastSegment.getArrivalTime();
        this.flightNumber = firstSegment.getFlightNumber();

        this.totalDuration = null;
        this.airlineName = this.airline; // Default to code
        this.aircraftName = "N/A";
    }

    /**
     * Creates a new Flight with explicit duration.
     *
     * @param segments      List of flight segments
     * @param totalDuration Total journey duration (ISO 8601)
     */
    public Flight(List<FlightSegment> segments, String totalDuration) {
        if (segments == null || segments.isEmpty()) {
            throw new IllegalArgumentException("Flight must have at least one segment");
        }

        this.segments = segments;

        FlightSegment firstSegment = segments.get(0);
        FlightSegment lastSegment = segments.get(segments.size() - 1);

        this.airline = firstSegment.getCarrierCode();
        this.origin = firstSegment.getOrigin();
        this.destination = lastSegment.getDestination();
        this.departureTime = firstSegment.getDepartureTime();
        this.arrivalTime = lastSegment.getArrivalTime();
        this.flightNumber = firstSegment.getFlightNumber();
        this.totalDuration = totalDuration;
        this.airlineName = this.airline; // Default to code
        this.aircraftName = "N/A";
    }

    /**
     * Creates a new Flight with all fields explicitly provided.
     * This constructor is primarily used for reconstructing Flight objects from
     * database records.
     *
     * @param flightNumber  Flight number
     * @param airline       Airline/carrier code
     * @param origin        Origin airport IATA code
     * @param destination   Destination airport IATA code
     * @param departureTime Departure time
     * @param arrivalTime   Arrival time
     * @param totalDuration Total journey duration (ISO 8601)
     * @param segments      List of flight segments
     */
    public Flight(String flightNumber, String airline, String origin, String destination,
            String departureTime, String arrivalTime, String totalDuration,
            List<FlightSegment> segments) {
        if (segments == null || segments.isEmpty()) {
            throw new IllegalArgumentException("Flight must have at least one segment");
        }

        this.flightNumber = flightNumber;
        this.airline = airline;
        this.origin = origin;
        this.destination = destination;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.totalDuration = totalDuration;
        this.segments = segments;
        this.airlineName = airline;
        this.aircraftName = "N/A";
    }

    public String getAirline() {
        return airline;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public String getTotalDuration() {
        return totalDuration;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public List<FlightSegment> getSegments() {
        return segments;
    }

    public int getNumberOfStops() {
        return segments.size() - 1;
    }

    public boolean isDirect() {
        return segments.size() == 1;
    }

    /** @deprecated Use getAirline() instead */
    @Deprecated
    public String getCompanhiaAerea() {
        return airline;
    }

    /** @deprecated Use getOrigin() instead */
    @Deprecated
    public String getOrigem() {
        return origin;
    }

    /** @deprecated Use getDestination() instead */
    @Deprecated
    public String getDestino() {
        return destination;
    }

    /** @deprecated Use getDepartureTime() instead */
    @Deprecated
    public String getDataPartida() {
        return departureTime;
    }

    /** @deprecated Use getArrivalTime() instead */
    @Deprecated
    public String getDataChegada() {
        return arrivalTime;
    }

    /** @deprecated Use getTotalDuration() instead */
    @Deprecated
    public String getDuracaoTotal() {
        return totalDuration;
    }

    /** @deprecated Use getNumberOfStops() instead */
    @Deprecated
    public int getEscalas() {
        return getNumberOfStops();
    }

    /** @deprecated Use getSegments() instead */
    @Deprecated
    public List<FlightSegment> getListaVoos() {
        return segments;
    }

    public String getAirlineName() {
        return airlineName != null ? airlineName : airline;
    }

    public void setAirlineName(String airlineName) {
        this.airlineName = airlineName;
    }

    public String getAircraftName() {
        return aircraftName != null ? aircraftName : "N/A";
    }

    public void setAircraftName(String aircraftName) {
        this.aircraftName = aircraftName;
    }

    @Override
    public String toString() {
        return String.format("Flight %s: %s → %s (%s) | %d stop(s) | Duration: %s",
                flightNumber, origin, destination, airline, getNumberOfStops(), totalDuration);
    }
}
