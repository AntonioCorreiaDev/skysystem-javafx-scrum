package pt.isec.skysystem.model.data;

import java.time.LocalDateTime;

public class TripNotification {
    private int id;
    private String tripId;
    private String message;
    private LocalDateTime timestamp;

    public TripNotification(int id, String tripId, String message, LocalDateTime timestamp) {
        this.id = id;
        this.tripId = tripId;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return "[" + timestamp.toLocalDate() + " " + timestamp.toLocalTime().toString().substring(0,5) + "] " + message;
    }
}