-- Migration V2 to V3
-- Add airline_name and aircraft_name to FLIGHTS table

ALTER TABLE FLIGHTS ADD COLUMN airline_name VARCHAR(100);
ALTER TABLE FLIGHTS ADD COLUMN aircraft_name VARCHAR(100);
