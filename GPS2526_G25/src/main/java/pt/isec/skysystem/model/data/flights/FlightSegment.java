package pt.isec.skysystem.model.data.flights;

/**
 * Represents a single flight segment (one leg of a journey).
 * A flight may consist of multiple segments if there are layovers/connections.
 */
public class FlightSegment {

    private final String flightNumber;
    private final String carrierCode;
    private final String origin;
    private final String destination;
    private final String departureTime;
    private final String arrivalTime;
    private final String duration;
    private final String aircraftCode;

    /**
     * Creates a new flight segment.
     *
     * @param flightNumber  Flight number
     * @param carrierCode   Airline carrier code
     * @param origin        IATA code of departure airport
     * @param destination   IATA code of arrival airport
     * @param departureTime ISO 8601 datetime string for departure
     * @param arrivalTime   ISO 8601 datetime string for arrival
     * @param duration      ISO 8601 duration string
     * @param aircraftCode  Aircraft equipment code
     */
    public FlightSegment(String flightNumber, String carrierCode, String origin, String destination,
            String departureTime, String arrivalTime, String duration, String aircraftCode) {
        this.flightNumber = flightNumber;
        this.carrierCode = carrierCode;
        this.origin = origin;
        this.destination = destination;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.duration = duration;
        this.aircraftCode = aircraftCode;
    }

    /**
     * @deprecated Legacy constructor without aircraft code.
     */
    public FlightSegment(String flightNumber, String carrierCode, String origin, String destination,
            String departureTime, String arrivalTime, String duration) {
        this(flightNumber, carrierCode, origin, destination, departureTime, arrivalTime, duration, null);
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public String getCarrierCode() {
        return carrierCode;
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

    public String getDuration() {
        return duration;
    }

    public String getAircraftCode() {
        return aircraftCode;
    }

    // --- LEGACY COMPATIBILITY (for gradual migration from NrVoos) ---

    /** @deprecated Use getFlightNumber() instead */
    @Deprecated
    public String getNumeroVoo() {
        return flightNumber;
    }

    /** @deprecated Use getCarrierCode() instead */
    @Deprecated
    public String getCompanhiaAerea() {
        return carrierCode;
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

    /** @deprecated Use getDuration() instead */
    @Deprecated
    public String getDuracao() {
        return duration;
    }

    @Override
    public String toString() {
        return String.format("\t[%s] %s (%s) -> %s (%s) | Departure: %s | Arrival: %s | Duration: %s",
                flightNumber,
                origin,
                carrierCode,
                destination,
                departureTime.split("T")[0], // Date only
                departureTime.split("T")[1], // Time only (departure)
                arrivalTime.split("T")[1], // Time only (arrival)
                duration);
    }
}
