-- Migration Script: FAVORITES_FLIGHTS → SAVED_TRIPS + FLIGHTS
-- Version: 1.0 to 2.0
-- Date: 2024-12-14
-- Description: Migrates from denormalized FAVORITES_FLIGHTS to normalized SAVED_TRIPS + FLIGHTS

-- ==========================================================
-- MIGRATION STEPS
-- ==========================================================

PRAGMA foreign_keys = OFF;

-- Step 1: Create new FLIGHTS table
CREATE TABLE IF NOT EXISTS FLIGHTS (
    flight_id INTEGER PRIMARY KEY AUTOINCREMENT,
    flight_number VARCHAR(20) NOT NULL,
    airline VARCHAR(50) NOT NULL,
    origin_iata VARCHAR(3) NOT NULL,
    destination_iata VARCHAR(3) NOT NULL,
    departure_time DATETIME NOT NULL,
    arrival_time DATETIME NOT NULL,
    total_duration VARCHAR(20) NOT NULL,
    number_of_stops INTEGER DEFAULT 0,
    segments_json TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Step 2: Create new SAVED_TRIPS table
CREATE TABLE IF NOT EXISTS SAVED_TRIPS (
    trip_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    total_price VARCHAR(20) NOT NULL,
    is_round_trip BOOLEAN DEFAULT 0,
    origin_iata VARCHAR(3) NOT NULL,
    destination_iata VARCHAR(3) NOT NULL,
    outbound_flight_id INTEGER NOT NULL,
    return_flight_id INTEGER,
    saved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES USER(user_id) ON DELETE CASCADE,
    FOREIGN KEY (outbound_flight_id) REFERENCES FLIGHTS(flight_id) ON DELETE CASCADE,
    FOREIGN KEY (return_flight_id) REFERENCES FLIGHTS(flight_id) ON DELETE CASCADE
);

-- Step 3: Migrate outbound flights from FAVORITES_FLIGHTS
INSERT INTO FLIGHTS (
    flight_number, airline, origin_iata, destination_iata,
    departure_time, arrival_time, total_duration, number_of_stops, segments_json
)
SELECT 
    ida_flight_number,
    ida_airline,
    origin_iata,
    destination_iata,
    ida_departure_date,
    ida_arrival_date,
    ida_duration,
    0, -- We'll calculate this from segments if needed
    ida_segments_json
FROM FAVORITES_FLIGHTS
WHERE ida_flight_number IS NOT NULL;

-- Step 4: Migrate return flights from FAVORITES_FLIGHTS (only for round trips)
INSERT INTO FLIGHTS (
    flight_number, airline, origin_iata, destination_iata,
    departure_time, arrival_time, total_duration, number_of_stops, segments_json
)
SELECT 
    volta_flight_number,
    volta_airline,
    destination_iata, -- Return flight goes back
    origin_iata,      -- Return flight goes back
    volta_departure_date,
    volta_arrival_date,
    volta_duration,
    COALESCE(volta_stops, 0),
    volta_segments_json
FROM FAVORITES_FLIGHTS
WHERE is_round_trip = 1 AND volta_flight_number IS NOT NULL;

-- Step 5: Migrate trips (complex - needs to link to created flights)
-- This uses a temporary mapping to link old favorite_flight_id to new flight_ids
INSERT INTO SAVED_TRIPS (
    user_id, total_price, is_round_trip, origin_iata, destination_iata,
    outbound_flight_id, return_flight_id, saved_at
)
SELECT 
    ff.user_id,
    ff.price_total,
    ff.is_round_trip,
    ff.origin_iata,
    ff.destination_iata,
    (SELECT f.flight_id FROM FLIGHTS f 
     WHERE f.flight_number = ff.ida_flight_number 
     AND f.departure_time = ff.ida_departure_date 
     LIMIT 1) as outbound_flight_id,
    CASE 
        WHEN ff.is_round_trip = 1 THEN
            (SELECT f.flight_id FROM FLIGHTS f 
             WHERE f.flight_number = ff.volta_flight_number 
             AND f.departure_time = ff.volta_departure_date 
             LIMIT 1)
        ELSE NULL
    END as return_flight_id,
    ff.saved_at
FROM FAVORITES_FLIGHTS ff;

-- Step 6: Drop old trigger
DROP TRIGGER IF EXISTS prevent_favorite_saved_at_update;

-- Step 7: Create new trigger for SAVED_TRIPS
CREATE TRIGGER prevent_saved_trip_timestamp_update
    BEFORE UPDATE ON SAVED_TRIPS
    FOR EACH ROW
    WHEN NEW.saved_at IS NOT OLD.saved_at
BEGIN
    SELECT RAISE(ABORT, 'The saved_at field in SAVED_TRIPS table cannot be modified.');
END;

-- Step 8: Drop old table (ONLY after verifying migration success)
-- DROP TABLE FAVORITES_FLIGHTS;

PRAGMA foreign_keys = ON;

-- ==========================================================
-- VERIFICATION QUERIES
-- ==========================================================

-- Verify migration counts
-- SELECT 'FAVORITES_FLIGHTS count' as check_name, COUNT(*) as count FROM FAVORITES_FLIGHTS
-- UNION ALL
-- SELECT 'FLIGHTS count', COUNT(*) FROM FLIGHTS
-- UNION ALL
-- SELECT 'SAVED_TRIPS count', COUNT(*) FROM SAVED_TRIPS;

-- Verify no orphaned trips
-- SELECT COUNT(*) as orphaned_trips 
-- FROM SAVED_TRIPS st
-- LEFT JOIN FLIGHTS f ON st.outbound_flight_id = f.flight_id
-- WHERE f.flight_id IS NULL;

-- ==========================================================
-- ROLLBACK INSTRUCTIONS
-- ==========================================================

-- If migration fails, run:
-- DROP TABLE IF EXISTS SAVED_TRIPS;
-- DROP TABLE IF EXISTS FLIGHTS;
-- DROP TRIGGER IF EXISTS prevent_saved_trip_timestamp_update;
