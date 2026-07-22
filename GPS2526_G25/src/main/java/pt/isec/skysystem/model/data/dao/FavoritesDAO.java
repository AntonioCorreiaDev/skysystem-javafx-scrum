package pt.isec.skysystem.model.data.dao;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import pt.isec.skysystem.model.data.DataBaseManager;
import pt.isec.skysystem.model.data.flights.Trip;
import pt.isec.skysystem.model.data.flights.Flight;
import pt.isec.skysystem.model.data.flights.FlightSegment;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class FavoritesDAO {

    private final Gson gson;

    public FavoritesDAO() {
        this.gson = new Gson();
    }

    /**
     * Saves a Trip object to the database.
     */
    public boolean saveFavoriteFlight(long userId, Trip trip) {

        String sql = "INSERT INTO FAVORITES_FLIGHTS (" +
                "user_id, ida_flight_number, price_total, origin_iata, destination_iata, " +
                "ida_airline, ida_departure_date, ida_arrival_date, ida_duration, ida_segments_json, " +
                "is_round_trip, volta_airline, volta_departure_date, volta_arrival_date, volta_duration, volta_stops, volta_segments_json, volta_flight_number"
                +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DataBaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);
            pstmt.setString(2, trip.getOutboundFlight().getFlightNumber());
            pstmt.setString(3, String.valueOf(trip.getTotalPrice()));
            pstmt.setString(4, trip.getOutboundFlight().getOrigin());
            pstmt.setString(5, trip.getOutboundFlight().getDestination());

            Flight outbound = trip.getOutboundFlight();
            pstmt.setString(6, outbound.getAirline());
            pstmt.setString(7, outbound.getDepartureTime());
            pstmt.setString(8, outbound.getArrivalTime());
            pstmt.setString(9, outbound.getTotalDuration());
            pstmt.setString(10, gson.toJson(outbound.getSegments()));

            pstmt.setBoolean(11, trip.isRoundTrip());

            if (trip.isRoundTrip()) {
                Flight returnFlight = trip.getReturnFlight();
                pstmt.setString(12, returnFlight.getAirline());
                pstmt.setString(13, returnFlight.getDepartureTime());
                pstmt.setString(14, returnFlight.getArrivalTime());
                pstmt.setString(15, returnFlight.getTotalDuration());
                pstmt.setInt(16, returnFlight.getNumberOfStops());
                pstmt.setString(17, gson.toJson(returnFlight.getSegments()));
                pstmt.setString(18, returnFlight.getFlightNumber());
            } else {
                pstmt.setString(12, null);
                pstmt.setString(13, null);
                pstmt.setString(14, null);
                pstmt.setString(15, null);
                pstmt.setInt(16, 0);
                pstmt.setString(17, null);
                pstmt.setString(18, null);
            }

            return pstmt.executeUpdate() > 0;

        } catch (Exception e) {
            System.err.println("Error saving favorite flight: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves all favorite flights and reconstructs Trip objects.
     */
    public List<Trip> getFavoriteFlights(long userId) {
        List<Trip> favoriteTrips = new ArrayList<>();

        String sql = "SELECT * FROM FAVORITES_FLIGHTS WHERE user_id = ?";

        try (Connection conn = DataBaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {

                    String jsonOutbound = rs.getString("ida_segments_json");
                    String jsonReturn = rs.getString("volta_segments_json");

                    Type listType = new TypeToken<ArrayList<FlightSegment>>() {
                    }.getType();
                    List<FlightSegment> outboundSegments = gson.fromJson(jsonOutbound, listType);
                    List<FlightSegment> returnSegments = gson.fromJson(jsonReturn, listType);

                    Flight outboundFlight = new Flight(
                            outboundSegments,
                            rs.getString("ida_duration"));

                    boolean isRoundTrip = rs.getBoolean("is_round_trip");
                    Flight returnFlight = null;

                    if (isRoundTrip && returnSegments != null) {
                        returnFlight = new Flight(
                                returnSegments,
                                rs.getString("volta_duration"));
                    }

                    double totalPrice = Double.parseDouble(rs.getString("price_total"));
                    Trip trip;

                    if (isRoundTrip && returnFlight != null) {
                        trip = new Trip(
                                rs.getString("favorite_flight_id"),
                                totalPrice,
                                outboundFlight,
                                returnFlight);
                    } else {
                        trip = new Trip(
                                rs.getString("favorite_flight_id"),
                                totalPrice,
                                outboundFlight);
                    }

                    favoriteTrips.add(trip);
                }
            }
        } catch (Exception e) {
            System.err.println("Error retrieving favorite flights: " + e.getMessage());
            e.printStackTrace();
        }
        return favoriteTrips;
    }

    /**
     * Removes a favorite flight for a specific user.
     */
    public boolean removeFavoriteFlight(long userId, String outboundFlightNumber, String returnFlightNumber) {
        if (returnFlightNumber != null) {
            String sql = "DELETE FROM FAVORITES_FLIGHTS WHERE user_id = ? AND ida_flight_number = ? AND volta_flight_number = ?";

            try (Connection conn = DataBaseManager.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setLong(1, userId);
                pstmt.setString(2, outboundFlightNumber);
                pstmt.setString(3, returnFlightNumber);

                int affectedRows = pstmt.executeUpdate();
                return affectedRows > 0;

            } catch (Exception e) {
                System.err.println("Error removing favorite flight: " + e.getMessage());
                return false;
            }
        }
        String sql = "DELETE FROM FAVORITES_FLIGHTS WHERE user_id = ? AND ida_flight_number = ?";

        try (Connection conn = DataBaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);
            pstmt.setString(2, outboundFlightNumber);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (Exception e) {
            System.err.println("Error removing favorite flight: " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks if a flight is already marked as favorite by the user.
     */
    public boolean isFavorite(long userId, String outboundFlightNumber, String returnFlightNumber) {
        if (returnFlightNumber != null) {
            String sql = "SELECT 1 FROM FAVORITES_FLIGHTS WHERE user_id = ? AND ida_flight_number = ? AND volta_flight_number = ?";

            try (Connection conn = DataBaseManager.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setLong(1, userId);
                pstmt.setString(2, outboundFlightNumber);
                pstmt.setString(3, returnFlightNumber);

                try (ResultSet rs = pstmt.executeQuery()) {
                    return rs.next();
                }
            } catch (Exception e) {
                System.err.println("Error checking favorite status: " + e.getMessage());
                return false;
            }
        } else {
            String sql = "SELECT 1 FROM FAVORITES_FLIGHTS WHERE user_id = ? AND ida_flight_number = ?";

            try (Connection conn = DataBaseManager.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setLong(1, userId);
                pstmt.setString(2, outboundFlightNumber);

                try (ResultSet rs = pstmt.executeQuery()) {
                    return rs.next();
                }
            } catch (Exception e) {
                System.err.println("Error checking favorite status: " + e.getMessage());
                return false;
            }
        }
    }
}
