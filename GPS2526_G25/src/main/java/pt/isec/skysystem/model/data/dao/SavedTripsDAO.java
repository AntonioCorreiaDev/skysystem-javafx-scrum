package pt.isec.skysystem.model.data.dao;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import pt.isec.skysystem.model.data.DataBaseManager;
import pt.isec.skysystem.model.data.TripNotification; // Importa a classe de cima
import pt.isec.skysystem.model.data.flights.Trip;
import pt.isec.skysystem.model.data.flights.Flight;
import pt.isec.skysystem.model.data.flights.FlightSegment;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for managing saved trips and their notifications.
 */
public class SavedTripsDAO {

    private final Gson gson;

    public SavedTripsDAO() {
        this.gson = new Gson();
    }

    /**
     * Saves a Trip to the database by inserting flights and creating trip record.
     */
    public boolean saveTrip(long userId, Trip trip) {
        String sql = "INSERT INTO SAVED_TRIPS (user_id, outbound_flight_id, return_flight_id, total_price, is_round_trip, origin_iata, destination_iata) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        // CORREÇÃO: Usar DataBaseManager.getConnection()
        try (Connection conn = DataBaseManager.getConnection()) {
            conn.setAutoCommit(false);

            try {
                long outboundFlightId = insertFlight(conn, trip.getOutboundFlight(), true);
                if (outboundFlightId == -1) {
                    conn.rollback();
                    return false;
                }

                Long returnFlightId = null;
                if (trip.isRoundTrip() && trip.getReturnFlight() != null) {
                    returnFlightId = insertFlight(conn, trip.getReturnFlight(), false);
                    if (returnFlightId == -1) {
                        conn.rollback();
                        return false;
                    }
                }

                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setLong(1, userId);
                    pstmt.setLong(2, outboundFlightId);
                    if (returnFlightId != null) {
                        pstmt.setLong(3, returnFlightId);
                    } else {
                        pstmt.setNull(3, java.sql.Types.BIGINT);
                    }
                    pstmt.setDouble(4, trip.getTotalPrice());
                    pstmt.setBoolean(5, trip.isRoundTrip());
                    pstmt.setString(6, trip.getOutboundFlight().getOrigin());
                    pstmt.setString(7, trip.getOutboundFlight().getDestination());

                    pstmt.executeUpdate();
                }

                conn.commit();
                return true;

            } catch (Exception e) {
                conn.rollback();
                System.err.println("Error saving trip: " + e.getMessage());
                e.printStackTrace();
                return false;
            }

        } catch (Exception e) {
            System.err.println("Error getting database connection: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private long insertFlight(Connection conn, Flight flight, boolean isOutbound) {
        String sql = "INSERT INTO FLIGHTS (flight_number, airline, airline_name, aircraft_name, origin_iata, destination_iata, "
                + "departure_time, arrival_time, total_duration, number_of_stops, segments_json) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, flight.getFlightNumber());
            pstmt.setString(2, flight.getAirline());
            pstmt.setString(3, flight.getAirlineName());
            pstmt.setString(4, flight.getAircraftName());
            pstmt.setString(5, flight.getOrigin());
            pstmt.setString(6, flight.getDestination());
            pstmt.setString(7, flight.getDepartureTime());
            pstmt.setString(8, flight.getArrivalTime());
            pstmt.setString(9, flight.getTotalDuration());
            pstmt.setInt(10, flight.getNumberOfStops());
            pstmt.setString(11, gson.toJson(flight.getSegments()));

            pstmt.executeUpdate();

            try (PreparedStatement lastIdStmt = conn.prepareStatement("SELECT last_insert_rowid()")) {
                try (ResultSet rs = lastIdStmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getLong(1);
                    }
                }
            }
            return -1;

        } catch (Exception e) {
            System.err.println("Error inserting flight: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    public List<Trip> getSavedTrips(long userId) {
        List<Trip> trips = new ArrayList<>();
        String sql = "SELECT " +
                "st.trip_id, st.total_price, st.is_round_trip, st.origin_iata, st.destination_iata, " +
                "of.flight_id as outbound_id, of.flight_number as outbound_number, of.airline as outbound_airline, " +
                "of.airline_name as outbound_airline_name, of.aircraft_name as outbound_aircraft_name, " +
                "of.origin_iata as outbound_origin, of.destination_iata as outbound_dest, " +
                "of.departure_time as outbound_dep, of.arrival_time as outbound_arr, " +
                "of.total_duration as outbound_duration, of.number_of_stops as outbound_stops, " +
                "of.segments_json as outbound_segments, " +
                "rf.flight_id as return_id, rf.flight_number as return_number, rf.airline as return_airline, " +
                "rf.airline_name as return_airline_name, rf.aircraft_name as return_aircraft_name, " +
                "rf.origin_iata as return_origin, rf.destination_iata as return_dest, " +
                "rf.departure_time as return_dep, rf.arrival_time as return_arr, " +
                "rf.total_duration as return_duration, rf.number_of_stops as return_stops, " +
                "rf.segments_json as return_segments " +
                "FROM SAVED_TRIPS st " +
                "INNER JOIN FLIGHTS of ON st.outbound_flight_id = of.flight_id " +
                "LEFT JOIN FLIGHTS rf ON st.return_flight_id = rf.flight_id " +
                "WHERE st.user_id = ?";

        // CORREÇÃO: Usar DataBaseManager.getConnection()
        try (Connection conn = DataBaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String outboundSegmentsJson = rs.getString("outbound_segments");
                    Type segmentListType = new TypeToken<List<FlightSegment>>() {}.getType();
                    List<FlightSegment> outboundSegments = gson.fromJson(outboundSegmentsJson, segmentListType);

                    Flight outboundFlight = new Flight(
                            rs.getString("outbound_number"),
                            rs.getString("outbound_airline"),
                            rs.getString("outbound_origin"),
                            rs.getString("outbound_dest"),
                            rs.getString("outbound_dep"),
                            rs.getString("outbound_arr"),
                            rs.getString("outbound_duration"),
                            outboundSegments);
                    outboundFlight.setAirlineName(rs.getString("outbound_airline_name"));
                    outboundFlight.setAircraftName(rs.getString("outbound_aircraft_name"));

                    Flight returnFlight = null;
                    boolean isRoundTrip = rs.getBoolean("is_round_trip");
                    if (isRoundTrip && rs.getString("return_segments") != null) {
                        String returnSegmentsJson = rs.getString("return_segments");
                        List<FlightSegment> returnSegments = gson.fromJson(returnSegmentsJson, segmentListType);

                        returnFlight = new Flight(
                                rs.getString("return_number"),
                                rs.getString("return_airline"),
                                rs.getString("return_origin"),
                                rs.getString("return_dest"),
                                rs.getString("return_dep"),
                                rs.getString("return_arr"),
                                rs.getString("return_duration"),
                                returnSegments);
                        returnFlight.setAirlineName(rs.getString("return_airline_name"));
                        returnFlight.setAircraftName(rs.getString("return_aircraft_name"));
                    }

                    double totalPrice = Double.parseDouble(rs.getString("total_price"));

                    // Cria a Trip normalmente. O ID será gerado internamente pelo construtor (ex: "TP123-TP124")
                    Trip trip = new Trip(outboundFlight, returnFlight, totalPrice);

                    // REMOVIDO: trip.setId(rs.getString("trip_id"));
                    // Não é necessário porque usamos o lookupTripDbId nas notificações.

                    trips.add(trip);
                }
            }
        } catch (Exception e) {
            System.err.println("Error retrieving saved trips: " + e.getMessage());
            e.printStackTrace();
        }
        return trips;
    }

    public boolean removeTrip(long userId, String outboundFlightNumber, String returnFlightNumber) {
        String sql = "DELETE FROM SAVED_TRIPS WHERE user_id = ? AND trip_id IN (" +
                "SELECT st.trip_id FROM SAVED_TRIPS st " +
                "INNER JOIN FLIGHTS of ON st.outbound_flight_id = of.flight_id " +
                "LEFT JOIN FLIGHTS rf ON st.return_flight_id = rf.flight_id " +
                "WHERE st.user_id = ? AND of.flight_number = ? " +
                (returnFlightNumber != null ? "AND rf.flight_number = ?" : "AND st.return_flight_id IS NULL") +
                ")";

        try (Connection conn = DataBaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);
            pstmt.setLong(2, userId);
            pstmt.setString(3, outboundFlightNumber);
            if (returnFlightNumber != null) {
                pstmt.setString(4, returnFlightNumber);
            }

            return pstmt.executeUpdate() > 0;

        } catch (Exception e) {
            System.err.println("Error removing trip: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean isTripSaved(long userId, String outboundFlightNumber, String returnFlightNumber) {
        String sql = "SELECT COUNT(*) FROM SAVED_TRIPS st " +
                "INNER JOIN FLIGHTS of ON st.outbound_flight_id = of.flight_id " +
                "LEFT JOIN FLIGHTS rf ON st.return_flight_id = rf.flight_id " +
                "WHERE st.user_id = ? AND of.flight_number = ? " +
                (returnFlightNumber != null ? "AND rf.flight_number = ?" : "AND st.return_flight_id IS NULL");

        try (Connection conn = DataBaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);
            pstmt.setString(2, outboundFlightNumber);
            if (returnFlightNumber != null) {
                pstmt.setString(3, returnFlightNumber);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (Exception e) {
            System.err.println("Error checking if trip is saved: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // =================================================================================
    //  NOVOS MÉTODOS PARA NOTIFICAÇÕES
    // =================================================================================

    /**
     * Guarda uma nova notificação/alteração para uma viagem específica.
     */
    public void addNotification(long userId, Trip trip, String message) {
        // 1. Descobrir o ID da BD para esta viagem
        long dbId = lookupTripDbId(userId, trip);

        if (dbId == -1) {
            System.err.println("Erro: Tentativa de guardar notificação para uma viagem não encontrada na BD.");
            return;
        }

        String sql = "INSERT INTO trip_notifications (trip_id, message) VALUES (?, ?)";

        try (Connection conn = DataBaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, dbId); // Usa o ID numérico da BD
            pstmt.setString(2, message);
            pstmt.executeUpdate();

        } catch (Exception e) {
            System.err.println("Erro ao guardar notificação: " + e.getMessage());
        }
    }

    /**
     * Obtém notificações, procurando automaticamente o ID da BD.
     */
    public List<TripNotification> getNotificationsForTrip(long userId, Trip trip) {
        List<TripNotification> list = new ArrayList<>();

        // 1. Descobrir o ID da BD para esta viagem
        long dbId = lookupTripDbId(userId, trip);
        if (dbId == -1) return list; // Viagem não encontrada

        String sql = "SELECT id, message, timestamp FROM trip_notifications WHERE trip_id = ? ORDER BY timestamp DESC";

        try (Connection conn = DataBaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, dbId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String timeStr = rs.getString("timestamp");
                    LocalDateTime timestamp;
                    try {
                        timestamp = LocalDateTime.parse(timeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    } catch (Exception e) {
                        try { timestamp = LocalDateTime.parse(timeStr); } catch (Exception ex) { timestamp = LocalDateTime.now(); }
                    }

                    // Nota: Passamos o ID interno como string no objeto TripNotification apenas para referência,
                    // ou podes passar o trip.getId() original se preferires.
                    list.add(new TripNotification(
                            rs.getInt("id"),
                            String.valueOf(dbId),
                            rs.getString("message"),
                            timestamp
                    ));
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao obter notificações: " + e.getMessage());
        }
        return list;
    }

    /**
     * Helper CRUCIAL: Encontra a Primary Key (trip_id) na tabela SAVED_TRIPS
     * baseando-se no User e nos Números de Voo do objeto Trip.
     */
    private long lookupTripDbId(long userId, Trip trip) {
        String outboundNum = trip.getOutboundFlight().getFlightNumber();
        String returnNum = trip.isRoundTrip() ? trip.getReturnFlight().getFlightNumber() : null;

        // Query complexa para achar o ID certo
        String sql = "SELECT st.trip_id FROM SAVED_TRIPS st " +
                "JOIN FLIGHTS of ON st.outbound_flight_id = of.flight_id " +
                "LEFT JOIN FLIGHTS rf ON st.return_flight_id = rf.flight_id " +
                "WHERE st.user_id = ? " +
                "AND of.flight_number = ? " +
                (returnNum != null ? "AND rf.flight_number = ?" : "AND st.return_flight_id IS NULL");

        try (Connection conn = DataBaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);
            pstmt.setString(2, outboundNum);
            if (returnNum != null) {
                pstmt.setString(3, returnNum);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("trip_id");
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao procurar ID da viagem: " + e.getMessage());
        }
        return -1; // Não encontrado
    }

}