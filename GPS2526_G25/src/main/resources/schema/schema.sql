-- Database Schema Version 4.0
-- Hybrid Approach: SAVED_TRIPS + FLIGHTS tables
-- Date: 2024-12-14

PRAGMA foreign_keys = ON;

-- ==========================================================
-- TABLES
-- ==========================================================

-- TABELA 1: USER (Utilizadores)
CREATE TABLE USER (
    user_id INTEGER PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    onboarding_completed BOOLEAN DEFAULT 0
);

-- TABELA 2: TRAVELPREFERENCES (Detalhes das Preferências do Utilizador)
CREATE TABLE TRAVELPREFERENCES (
    user_id INTEGER NOT NULL,
    vibe VARCHAR(50) NOT NULL,
    budget_level VARCHAR(50) NOT NULL,
    pace VARCHAR(50) NOT NULL,
    company VARCHAR(50) NOT NULL,
    climate VARCHAR(50) NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id),
    FOREIGN KEY (user_id) REFERENCES USER(user_id) ON DELETE CASCADE
);

-- TABELA 3: FLIGHTS (Individual Flights - Outbound or Return)
CREATE TABLE FLIGHTS (
    flight_id INTEGER PRIMARY KEY AUTOINCREMENT,
    
    -- Flight identification
    flight_number VARCHAR(20) NOT NULL,
    airline VARCHAR(50) NOT NULL,
    airline_name VARCHAR(100),
    aircraft_name VARCHAR(100),
    
    -- Route
    origin_iata VARCHAR(3) NOT NULL,
    destination_iata VARCHAR(3) NOT NULL,
    
    -- Timing
    departure_time DATETIME NOT NULL,
    arrival_time DATETIME NOT NULL,
    total_duration VARCHAR(20) NOT NULL,
    
    -- Stops and segments
    number_of_stops INTEGER DEFAULT 0,
    segments_json TEXT NOT NULL,
    
    -- Metadata
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- TABELA 4: SAVED_TRIPS (User's Saved Trips)
CREATE TABLE SAVED_TRIPS (
    trip_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    
    -- Trip metadata
    total_price VARCHAR(20) NOT NULL,
    is_round_trip BOOLEAN DEFAULT 0,
    
    -- Route information
    origin_iata VARCHAR(3) NOT NULL,
    destination_iata VARCHAR(3) NOT NULL,
    
    -- Foreign keys to flights
    outbound_flight_id INTEGER NOT NULL,
    return_flight_id INTEGER,
    
    -- Metadata
    saved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES USER(user_id) ON DELETE CASCADE,
    FOREIGN KEY (outbound_flight_id) REFERENCES FLIGHTS(flight_id) ON DELETE CASCADE,
    FOREIGN KEY (return_flight_id) REFERENCES FLIGHTS(flight_id) ON DELETE CASCADE
);

-- TABELA 5: SUGGESTIONS
CREATE TABLE SUGGESTIONS (
    suggestion_id INTEGER PRIMARY KEY,
    user_id INTEGER NOT NULL,
    destination_iata VARCHAR(3) NOT NULL,
    tags VARCHAR(255) NOT NULL,
    reason VARCHAR(255) NOT NULL,
    price_range VARCHAR(50) NOT NULL,
    image_url VARCHAR(255),
    saved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES USER(user_id) ON DELETE CASCADE
);

-- Tabela para notificações de alterações nos voos (V4)
CREATE TABLE IF NOT EXISTS trip_notifications (
                                                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                                                  trip_id INTEGER NOT NULL,
                                                  message TEXT NOT NULL,
                                                  timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
                                                  is_read BOOLEAN DEFAULT 0,
                                                  FOREIGN KEY(trip_id) REFERENCES SAVED_TRIPS(trip_id) ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_trip_notifications_trip_id ON trip_notifications(trip_id);

-- ==========================================================
-- TRIGGERS (para automação de datas e integridade)
-- ==========================================================

-- TRIGGER 1: Atualiza automaticamente o campo 'updated_at' na TRAVELPREFERENCES
CREATE TRIGGER update_travelpreferences_timestamp
    BEFORE UPDATE ON TRAVELPREFERENCES
    FOR EACH ROW
BEGIN
    UPDATE TRAVELPREFERENCES
    SET updated_at = strftime('%Y-%m-%d %H:%M:%S', 'now')
    WHERE user_id = NEW.user_id;
END;

-- TRIGGER 2: Evita a alteração do 'saved_at' na tabela SAVED_TRIPS
CREATE TRIGGER prevent_saved_trip_timestamp_update
    BEFORE UPDATE ON SAVED_TRIPS
    FOR EACH ROW
    WHEN NEW.saved_at IS NOT OLD.saved_at
BEGIN
    SELECT RAISE(ABORT, 'The saved_at field in SAVED_TRIPS table cannot be modified.');
END;

-- TRIGGER 3: Evita a alteração do 'saved_at' na tabela SUGGESTIONS
CREATE TRIGGER prevent_suggestion_saved_at_update
    BEFORE UPDATE ON SUGGESTIONS
    FOR EACH ROW
    WHEN NEW.saved_at IS NOT OLD.saved_at
BEGIN
    SELECT RAISE(ABORT, 'O campo saved_at na tabela SUGGESTIONS não pode ser modificado.');
END;