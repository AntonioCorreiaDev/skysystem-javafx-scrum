package pt.isec.skysystem.model.data.flights;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a complete travel itinerary (trip).
 * A trip always has an outbound flight and optionally a return flight.
 */
public class Trip {

    private final String id;
    private final double totalPrice;
    private final Flight outboundFlight;
    private final Flight returnFlight;

    /**
     * Creates a one-way trip.
     *
     * @param id             Unique trip identifier
     * @param totalPrice     Total price for the trip
     * @param outboundFlight Outbound flight
     */
    public Trip(String id, double totalPrice, Flight outboundFlight) {
        if (outboundFlight == null) {
            throw new IllegalArgumentException("Outbound flight is required");
        }
        this.id = id;
        this.totalPrice = totalPrice;
        this.outboundFlight = outboundFlight;
        this.returnFlight = null;
    }

    /**
     * Creates a round-trip.
     *
     * @param id             Unique trip identifier
     * @param totalPrice     Total price for the trip
     * @param outboundFlight Outbound flight
     * @param returnFlight   Return flight
     */
    public Trip(String id, double totalPrice, Flight outboundFlight, Flight returnFlight) {
        if (outboundFlight == null) {
            throw new IllegalArgumentException("Outbound flight is required");
        }
        this.id = id;
        this.totalPrice = totalPrice;
        this.outboundFlight = outboundFlight;
        this.returnFlight = returnFlight;
    }

    /**
     * Creates a one-way trip without explicit ID (generates ID from flight number).
     * Used primarily for database reconstruction.
     *
     * @param outboundFlight Outbound flight
     * @param totalPrice     Total price for the trip
     */
    public Trip(Flight outboundFlight, double totalPrice) {
        if (outboundFlight == null) {
            throw new IllegalArgumentException("Outbound flight is required");
        }
        this.id = outboundFlight.getFlightNumber();
        this.totalPrice = totalPrice;
        this.outboundFlight = outboundFlight;
        this.returnFlight = null;
    }

    /**
     * Creates a round-trip without explicit ID (generates ID from flight numbers).
     * Used primarily for database reconstruction.
     *
     * @param outboundFlight Outbound flight
     * @param returnFlight   Return flight (can be null for one-way)
     * @param totalPrice     Total price for the trip
     */
    public Trip(Flight outboundFlight, Flight returnFlight, double totalPrice) {
        if (outboundFlight == null) {
            throw new IllegalArgumentException("Outbound flight is required");
        }
        if (returnFlight != null) {
            this.id = outboundFlight.getFlightNumber() + "-" + returnFlight.getFlightNumber();
        } else {
            this.id = outboundFlight.getFlightNumber();
        }
        this.totalPrice = totalPrice;
        this.outboundFlight = outboundFlight;
        this.returnFlight = returnFlight;
    }

    public String getId() {
        return id;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public Flight getOutboundFlight() {
        return outboundFlight;
    }

    public Flight getReturnFlight() {
        return returnFlight;
    }

    public boolean isRoundTrip() {
        return returnFlight != null;
    }

    public boolean hasReturnFlight() {
        return returnFlight != null;
    }

    public String getOrigin() {
        return outboundFlight.getOrigin();
    }

    public String getDestination() {
        return outboundFlight.getDestination();
    }

    // --- LEGACY COMPATIBILITY (for gradual migration from Voo) ---

    /** @deprecated Use getTotalPrice() instead */
    @Deprecated
    public double getPrecoTotal() {
        return totalPrice;
    }

    /** @deprecated Use getTotalPrice() and format as string */
    @Deprecated
    public String getPrecoTotalString() {
        return String.valueOf(totalPrice);
    }

    /** @deprecated Use getOrigin() instead */
    @Deprecated
    public String getOrigem() {
        return getOrigin();
    }

    /** @deprecated Use getDestination() instead */
    @Deprecated
    public String getDestino() {
        return getDestination();
    }

    // --- Outbound flight convenience methods ---

    /** @deprecated Use getOutboundFlight().getAirline() instead */
    @Deprecated
    public String getIdaCompanhiaAerea() {
        return outboundFlight.getAirline();
    }

    /** @deprecated Use getOutboundFlight().getDepartureTime() instead */
    @Deprecated
    public String getDataPartidaInicial() {
        return outboundFlight.getDepartureTime();
    }

    /** @deprecated Use getOutboundFlight().getArrivalTime() instead */
    @Deprecated
    public String getDataChegadaFinal() {
        return outboundFlight.getArrivalTime();
    }

    /** @deprecated Use getOutboundFlight().getTotalDuration() instead */
    @Deprecated
    public String getDuracaoTotalViagem() {
        return outboundFlight.getTotalDuration();
    }

    /** @deprecated Use getOutboundFlight().getSegments() instead */
    @Deprecated
    public List<FlightSegment> getListaVoos() {
        return outboundFlight.getSegments();
    }

    /** @deprecated Use getOutboundFlight().getNumberOfStops() instead */
    @Deprecated
    public int getEscalas() {
        return outboundFlight.getNumberOfStops();
    }

    /** @deprecated Use getOutboundFlight().getFlightNumber() instead */
    @Deprecated
    public String getIdaNumeroVooInicial() {
        return outboundFlight.getFlightNumber();
    }

    // --- Return flight convenience methods ---

    /** @deprecated Use getReturnFlight().getAirline() instead */
    @Deprecated
    public String getReturnCompanhiaAerea() {
        return returnFlight != null ? returnFlight.getAirline() : null;
    }

    /** @deprecated Use getReturnFlight().getDepartureTime() instead */
    @Deprecated
    public String getReturnDataPartidaInicial() {
        return returnFlight != null ? returnFlight.getDepartureTime() : null;
    }

    /** @deprecated Use getReturnFlight().getArrivalTime() instead */
    @Deprecated
    public String getReturnDataChegadaFinal() {
        return returnFlight != null ? returnFlight.getArrivalTime() : null;
    }

    /** @deprecated Use getReturnFlight().getTotalDuration() instead */
    @Deprecated
    public String getReturnDuracaoTotalViagem() {
        return returnFlight != null ? returnFlight.getTotalDuration() : null;
    }

    /** @deprecated Use getReturnFlight().getNumberOfStops() instead */
    @Deprecated
    public int getReturnEscalas() {
        return returnFlight != null ? returnFlight.getNumberOfStops() : 0;
    }

    /** @deprecated Use getReturnFlight().getSegments() instead */
    @Deprecated
    public List<FlightSegment> getVoltaListaVoos() {
        return returnFlight != null ? returnFlight.getSegments() : new ArrayList<>();
    }

    /** @deprecated Use getReturnFlight().getFlightNumber() instead */
    @Deprecated
    public String getReturnNumeroVooInicial() {
        return returnFlight != null ? returnFlight.getFlightNumber() : null;
    }

    /** @deprecated Use getReturnFlight() != null instead */
    @Deprecated
    public String getVoltaDuracaoTotal() {
        return returnFlight != null ? returnFlight.getTotalDuration() : null;
    }

    /** @deprecated Use getReturnFlight().getSegments() instead */
    @Deprecated
    public List<FlightSegment> getReturnFlights() {
        return returnFlight != null ? returnFlight.getSegments() : new ArrayList<>();
    }

    @Override
    public String toString() {
        if (isRoundTrip()) {
            return String.format("Trip %s: %s ↔ %s | €%.2f | Round-trip",
                    id, getOrigin(), getDestination(), totalPrice);
        } else {
            return String.format("Trip %s: %s → %s | €%.2f | One-way",
                    id, getOrigin(), getDestination(), totalPrice);
        }
    }

    /**
     * Compares this trip to another object for equality.
     * Two trips are considered equal if they have the same:
     * - Outbound flight number
     * - Return flight number (or both null)
     *
     * @param obj The object to compare with
     * @return true if the trips are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Trip other = (Trip) obj;

        String thisOutboundFN = this.outboundFlight.getFlightNumber();
        String otherOutboundFN = other.outboundFlight.getFlightNumber();
        if (!thisOutboundFN.equals(otherOutboundFN)) {
            return false;
        }

        String thisReturnFN = this.returnFlight != null ? this.returnFlight.getFlightNumber() : null;
        String otherReturnFN = other.returnFlight != null ? other.returnFlight.getFlightNumber() : null;

        if (thisReturnFN == null && otherReturnFN == null) {
            return true;
        } else if (thisReturnFN == null || otherReturnFN == null) {
            return false;
        } else {
            return thisReturnFN.equals(otherReturnFN);
        }
    }

    /**
     * Returns a hash code for this trip.
     * The hash code is computed based on the outbound flight number
     * and return flight number (if present).
     * 
     * @return hash code for this trip
     */
    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + outboundFlight.getFlightNumber().hashCode();
        if (returnFlight != null) {
            result = 31 * result + returnFlight.getFlightNumber().hashCode();
        }
        return result;
    }
}
