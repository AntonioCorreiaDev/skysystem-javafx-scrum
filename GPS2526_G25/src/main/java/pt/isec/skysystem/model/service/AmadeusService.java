package pt.isec.skysystem.model.service;

import com.amadeus.Amadeus;
import com.amadeus.Params;
import com.amadeus.exceptions.ResponseException;
import com.amadeus.resources.FlightOfferSearch;
import pt.isec.skysystem.model.data.flights.Flight;
import pt.isec.skysystem.model.data.flights.FlightSegment;
import pt.isec.skysystem.model.data.flights.Trip;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class AmadeusService {

    public static class SearchResult {
        private final List<Trip> trips;
        private final Map<String, String> locations;

        public SearchResult(List<Trip> trips, Map<String, String> locations) {
            this.trips = trips;
            this.locations = locations;
        }

        public List<Trip> getTrips() {
            return trips;
        }

        public Map<String, String> getLocations() {
            return locations;
        }
    }

    private final Amadeus amadeus;
    private final Map<String, String> staticCityCache = new HashMap<>();

    public AmadeusService() {
        this.amadeus = Amadeus.builder("I8aqzGvAdDIazzUMnXFDZtAAoj5DK5GN", "ZysHaS2NswmBm3rA")
                .build();
        populateStaticCityCache();
    }

    private void populateStaticCityCache() {
        staticCityCache.put("LIS", "Lisbon");
        staticCityCache.put("OPO", "Porto");
        staticCityCache.put("CDG", "Paris");
        staticCityCache.put("ORY", "Paris");
        staticCityCache.put("MAD", "Madrid");
        staticCityCache.put("BCN", "Barcelona");
        staticCityCache.put("LON", "London");
        staticCityCache.put("LHR", "London");
        staticCityCache.put("LGW", "London");
        staticCityCache.put("NYC", "New York");
        staticCityCache.put("JFK", "New York");
        staticCityCache.put("EWR", "New York");
        staticCityCache.put("LGA", "New York");
        staticCityCache.put("BER", "Berlin");
        staticCityCache.put("ROM", "Rome");
        staticCityCache.put("FCO", "Rome");
        staticCityCache.put("AMS", "Amsterdam");
        staticCityCache.put("DUB", "Dublin");
        staticCityCache.put("BRU", "Brussels");
        staticCityCache.put("VIE", "Vienna");
        staticCityCache.put("ZRH", "Zurich");
        staticCityCache.put("CPH", "Copenhagen");
        staticCityCache.put("STO", "Stockholm");
        staticCityCache.put("ARN", "Stockholm");
        staticCityCache.put("HEL", "Helsinki");
        staticCityCache.put("OSL", "Oslo");
        staticCityCache.put("PRG", "Prague");
        staticCityCache.put("BUD", "Budapest");
        staticCityCache.put("WAW", "Warsaw");
        staticCityCache.put("ATH", "Athens");
        staticCityCache.put("IST", "Istanbul");
    }

    /**
     * Main method for flight search, supporting round-trip and direct flights.
     * 
     * @return A SearchResult object containing trips and location dictionary.
     */
    public SearchResult searchFlights(
            String origin,
            String destination,
            String departureDate,
            String returnDate,
            int nPassengers,
            boolean isDirect) throws ResponseException {

        Params params = Params.with("originLocationCode", origin)
                .and("destinationLocationCode", destination)
                .and("departureDate", departureDate)
                .and("adults", nPassengers)
                .and("currencyCode", "EUR")
                .and("max", 20);

        if (returnDate != null) {
            params = params.and("returnDate", returnDate);
        }
        if (isDirect) {
            params = params.and("nonStop", true);
        }

        try {
            FlightOfferSearch[] offers = amadeus.shopping.flightOffersSearch.get(params);

            Map<String, String> carrierNames = new HashMap<>();
            Map<String, String> aircraftNames = new HashMap<>();
            Map<String, String> locationNames = new HashMap<>();

            if (offers != null && offers.length > 0) {
                try {
                    com.amadeus.Response response = offers[0].getResponse();
                    if (response != null && response.getBody() != null) {
                        JsonObject json = JsonParser.parseString(response.getBody()).getAsJsonObject();
                        if (json.has("dictionaries")) {
                            JsonObject dictionaries = json.getAsJsonObject("dictionaries");

                            if (dictionaries.has("carriers")) {
                                JsonObject carriers = dictionaries.getAsJsonObject("carriers");
                                for (String key : carriers.keySet()) {
                                    carrierNames.put(key, carriers.get(key).getAsString());
                                }
                            }

                            if (dictionaries.has("aircraft")) {
                                JsonObject aircraft = dictionaries.getAsJsonObject("aircraft");
                                for (String key : aircraft.keySet()) {
                                    aircraftNames.put(key, aircraft.get(key).getAsString());
                                }
                            }

                            if (dictionaries.has("locations")) {
                                JsonObject locations = dictionaries.getAsJsonObject("locations");
                                for (String key : locations.keySet()) {
                                    JsonObject locationInfo = locations.getAsJsonObject(key);

                                    if (locationInfo.has("detailedName")) {
                                        locationNames.put(key,
                                                toTitleCase(locationInfo.get("detailedName").getAsString()));
                                    } else if (locationInfo.has("name")) {
                                        locationNames.put(key, toTitleCase(locationInfo.get("name").getAsString()));
                                    } else if (locationInfo.has("cityCode")) {
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing dictionaries: " + e.getMessage());
                }
            }

            List<Trip> trips = processOffers(offers, carrierNames, aircraftNames);
            return new SearchResult(trips, locationNames);
        } catch (ResponseException e) {
            System.err.println("[ERROR] Amadeus API Error:");
            System.err.println("  Status Code: " + e.getCode());
            System.err.println("  Description: " + e.getDescription());
            if (e.getResponse() != null) {
                System.err.println("  Response Body: " + e.getResponse().getBody());
                System.err.println("  Response Result: " + e.getResponse().getResult());
            }

            System.out.println("[FALLBACK] Using mock flight data due to API error");
            return generateMockFlights(origin, destination, departureDate, returnDate, nPassengers, isDirect);
        }
    }

    private List<Trip> processOffers(FlightOfferSearch[] offers, Map<String, String> carrierNames,
            Map<String, String> aircraftNames) {
        if (offers == null || offers.length == 0) {
            return new ArrayList<>();
        }

        List<Trip> trips = new ArrayList<>();

        for (FlightOfferSearch offer : offers) {
            FlightOfferSearch.Itinerary outboundItinerary = offer.getItineraries()[0];
            List<FlightSegment> outboundSegments = extractSegments(outboundItinerary);
            String outboundDuration = outboundItinerary.getDuration().replaceFirst("PT", "");

            Flight outboundFlight = new Flight(outboundSegments, outboundDuration);
            if (!outboundSegments.isEmpty()) {
                String carrierCode = outboundFlight.getAirline();
                String aircraftCode = outboundSegments.get(0).getAircraftCode(); // From first segment

                outboundFlight.setAirlineName(toTitleCase(carrierNames.getOrDefault(carrierCode, carrierCode)));
                outboundFlight.setAircraftName(aircraftNames.getOrDefault(aircraftCode, aircraftCode));
            }

            Flight returnFlight = null;
            boolean isRoundTrip = offer.getItineraries().length > 1;

            if (isRoundTrip) {
                FlightOfferSearch.Itinerary returnItinerary = offer.getItineraries()[1];
                List<FlightSegment> returnSegments = extractSegments(returnItinerary);
                String returnDuration = returnItinerary.getDuration().replaceFirst("PT", "").toLowerCase();

                returnFlight = new Flight(returnSegments, returnDuration);
                if (!returnSegments.isEmpty()) {
                    String carrierCode = returnFlight.getAirline();
                    String aircraftCode = returnSegments.get(0).getAircraftCode(); // From first segment

                    returnFlight.setAirlineName(toTitleCase(carrierNames.getOrDefault(carrierCode, carrierCode)));
                    returnFlight.setAircraftName(aircraftNames.getOrDefault(aircraftCode, aircraftCode));
                }
            }

            double totalPrice = Double.parseDouble(offer.getPrice().getTotal());
            Trip trip;

            if (isRoundTrip) {
                trip = new Trip(offer.getId(), totalPrice, outboundFlight, returnFlight);
            } else {
                trip = new Trip(offer.getId(), totalPrice, outboundFlight);
            }

            trips.add(trip);
        }

        return trips;
    }

    /**
     * Helper method to extract segments (flight legs) from an itinerary.
     */
    private List<FlightSegment> extractSegments(FlightOfferSearch.Itinerary itinerary) {
        List<FlightSegment> segments = new ArrayList<>();

        for (FlightOfferSearch.SearchSegment segment : itinerary.getSegments()) {
            FlightSegment flightSegment = new FlightSegment(
                    segment.getCarrierCode() + segment.getNumber(),
                    segment.getCarrierCode(),
                    segment.getDeparture().getIataCode(),
                    segment.getArrival().getIataCode(),
                    segment.getDeparture().getAt(),
                    segment.getArrival().getAt(),
                    segment.getDuration(),
                    segment.getAircraft() != null ? segment.getAircraft().getCode() : null);
            segments.add(flightSegment);
        }
        return segments;
    }

    public String searchLocations(String keyword) throws ResponseException {

        if (keyword == null || keyword.trim().length() < 3) {
            return null;
        }
        String cleanKeyword = keyword.trim().toUpperCase();

        Params params = Params.with("keyword", cleanKeyword)
                .and("subType", "CITY,AIRPORT");

        com.amadeus.resources.Location[] locations = amadeus.referenceData.locations.get(params);

        if (locations == null || locations.length == 0) {
            return null;
        }
        String iataCode = locations[0].getIataCode();

        if (iataCode == null || iataCode.trim().isEmpty()) {
            return null;
        }

        return iataCode.trim().toUpperCase();
    }

    public String getLocationName(String iataCode) {
        if (iataCode == null || iataCode.trim().length() < 3) {
            return iataCode;
        }

        String upperCode = iataCode.toUpperCase().trim();
        if (staticCityCache.containsKey(upperCode)) {
            return staticCityCache.get(upperCode);
        }
        try {
            Params params = Params.with("keyword", iataCode)
                    .and("subType", "CITY,AIRPORT");

            com.amadeus.resources.Location[] locations = amadeus.referenceData.locations.get(params);

            if (locations != null) {
                for (com.amadeus.resources.Location location : locations) {
                    if (location.getIataCode() != null && location.getIataCode().equalsIgnoreCase(iataCode)) {
                        if (location.getAddress() != null) {
                            return toTitleCase(location.getAddress().getCityName());
                        }
                    }
                }
            }
        } catch (ResponseException e) {
            System.err.println("Error getting location name: " + e.getMessage());
        }
        return iataCode; // fallback
    }

    private String toTitleCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        String[] words = input.toLowerCase().split("\\s+");
        StringBuilder titleCase = new StringBuilder();
        for (String word : words) {
            if (word.length() > 0) {
                titleCase.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }
        return titleCase.toString().trim();
    }

    // ==========================MOCK DATA GENERATION===============================

    /**
     * Generates mock flight data for testing when the Amadeus API is unavailable.
     */
    private SearchResult generateMockFlights(String origin, String destination, String departureDate,
            String returnDate, int nPassengers, boolean isDirect) {
        List<Trip> mockTrips = new ArrayList<>();
        int numFlights = isDirect ? 3 : 6;

        for (int i = 0; i < numFlights; i++) {
            // Generate random departure time
            String outboundDeparture = generateRandomTime(departureDate, i);
            String duration = generateRandomDuration(i);
            String outboundArrival = addDuration(outboundDeparture, duration);

            List<FlightSegment> outboundSegments = new ArrayList<>();
            if (isDirect || i % 2 == 0) {
                outboundSegments.add(new FlightSegment(
                        "TP" + (100 + i), "TAP", origin, destination,
                        outboundDeparture, outboundArrival, duration, "320"));
            } else {
                String stopover = "MAD";
                String stopoverArrival = addDuration(outboundDeparture, halfDuration(duration));
                String layoverDeparture = addMinutes(stopoverArrival, 45);

                outboundSegments.add(new FlightSegment(
                        "TP" + (100 + i), "TAP", origin, stopover,
                        outboundDeparture, stopoverArrival, halfDuration(duration), "320"));
                outboundSegments.add(new FlightSegment(
                        "TP" + (200 + i), "TAP", stopover, destination,
                        layoverDeparture, outboundArrival, halfDuration(duration), "320"));
            }

            Flight outboundFlight = new Flight(outboundSegments, duration);
            Flight returnFlight = null;

            if (returnDate != null) {
                String returnDeparture = generateRandomTime(returnDate, i + 3);
                String returnArrival = addDuration(returnDeparture, duration);

                List<FlightSegment> returnSegments = new ArrayList<>();
                if (isDirect || i % 2 == 0) {
                    returnSegments.add(new FlightSegment(
                            "TP" + (300 + i), "TAP", destination, origin,
                            returnDeparture, returnArrival, duration, "320"));
                } else {
                    String stopover = "MAD";
                    String returnStopoverArrival = addDuration(returnDeparture, halfDuration(duration));
                    String returnLayoverDeparture = addMinutes(returnStopoverArrival, 45);

                    returnSegments.add(new FlightSegment(
                            "TP" + (300 + i), "TAP", destination, stopover,
                            returnDeparture, returnStopoverArrival, halfDuration(duration), "320"));
                    returnSegments.add(new FlightSegment(
                            "TP" + (400 + i), "TAP", stopover, origin,
                            returnLayoverDeparture, returnArrival, halfDuration(duration), "320"));
                }
                returnFlight = new Flight(returnSegments, duration);
            }

            double basePrice = 100.0;
            double price = (basePrice + (i * 20) + (outboundSegments.size() > 1 ? 30 : 0)) * nPassengers;
            if (returnFlight != null)
                price *= 1.8;

            mockTrips.add(new Trip(outboundFlight, returnFlight, price));
        }
        return new SearchResult(mockTrips, Collections.emptyMap());
    }

    private String generateRandomTime(String date, int seed) {
        int[] hours = { 6, 9, 12, 14, 17, 20 };
        int[] minutes = { 0, 15, 30, 45 };
        int hour = hours[seed % hours.length];
        int minute = minutes[seed % minutes.length];

        // Handle hour overflow for dates
        int days = hour / 24;
        int adjustedHour = hour % 24;
        String dateToUse = date;
        if (days > 0) {
            try {
                java.time.LocalDate localDate = java.time.LocalDate.parse(date);
                dateToUse = localDate.plusDays(days).toString();
            } catch (Exception e) {
            }
        }

        return dateToUse + "T" + String.format("%02d:%02d", adjustedHour, minute);
    }

    private String generateRandomDuration(int seed) {
        String[] durations = { "2H", "2H15M", "2H30M", "2H45M", "3H", "3H15M" };
        return durations[seed % durations.length];
    }

    private String halfDuration(String duration) {
        int hours = Integer.parseInt(duration.split("H")[0]);
        int mins = duration.contains("M") ? Integer.parseInt(duration.split("H")[1].replace("M", "")) : 0;
        int totalMins = hours * 60 + mins;
        int halfMins = totalMins / 2;
        return (halfMins / 60) + "H" + (halfMins % 60) + "M";
    }

    private String addDuration(String time, String duration) {
        String[] parts = time.split("T");
        String date = parts[0];
        String[] timeParts = parts[1].split(":");
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);

        int durationHours = Integer.parseInt(duration.split("H")[0]);
        int durationMins = duration.contains("M") ? Integer.parseInt(duration.split("H")[1].replace("M", "")) : 0;

        minute += durationMins;
        hour += durationHours + (minute / 60);
        minute %= 60;

        // Handle day overflow
        int days = hour / 24;
        int adjustedHour = hour % 24;
        String dateToUse = date;
        if (days > 0) {
            try {
                java.time.LocalDate localDate = java.time.LocalDate.parse(date);
                dateToUse = localDate.plusDays(days).toString();
            } catch (Exception e) {
            }
        }

        return dateToUse + "T" + String.format("%02d:%02d", adjustedHour, minute);
    }

    private String addMinutes(String time, int minutesToAdd) {
        String[] parts = time.split("T");
        String date = parts[0];
        String[] timeParts = parts[1].split(":");
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);

        minute += minutesToAdd;
        hour += minute / 60;
        minute %= 60;

        // Handle day overflow
        int days = hour / 24;
        int adjustedHour = hour % 24;
        String dateToUse = date;
        if (days > 0) {
            try {
                java.time.LocalDate localDate = java.time.LocalDate.parse(date);
                dateToUse = localDate.plusDays(days).toString();
            } catch (Exception e) {
            }
        }

        return dateToUse + "T" + String.format("%02d:%02d", adjustedHour, minute);
    }

    /**
     * Obtém o estado mais recente de uma Trip guardada.
     * @param savedTrip A viagem guardada na BD.
     * @param forceMock Se true, gera dados simulados para teste.
     * @return A Trip atualizada ou null.
     */
    public Trip getLatestTripStatus(Trip savedTrip, boolean forceMock) {
        // 1. Mock Mode
        if (forceMock) {
            return generateMockTripUpdate(savedTrip);
        }

        // 2. Real API Mode
        try {
            // Extrair dados da pesquisa original
            String origin = savedTrip.getOutboundFlight().getOrigin();
            String dest = savedTrip.getOutboundFlight().getDestination();

            // Tratamento de segurança para datas (caso venha com hora T)
            String depDate = savedTrip.getOutboundFlight().getDepartureTime();
            if (depDate.contains("T")) depDate = depDate.split("T")[0];

            String retDate = null;
            if (savedTrip.isRoundTrip() && savedTrip.getReturnFlight() != null) {
                retDate = savedTrip.getReturnFlight().getDepartureTime();
                if (retDate.contains("T")) retDate = retDate.split("T")[0];
            }

            // --- CORREÇÃO DO ERRO DE CAST AQUI ---
            // searchFlights devolve SearchResult, não List<Trip>
            SearchResult result = searchFlights(origin, dest, depDate, retDate, 1, false);
            List<Trip> currentOffers = result.getTrips();
            // -------------------------------------

            // Tentar encontrar a mesma viagem na lista (pelo número de voo)
            String targetOutboundNum = savedTrip.getOutboundFlight().getFlightNumber();
            String targetReturnNum = savedTrip.isRoundTrip() && savedTrip.getReturnFlight() != null
                    ? savedTrip.getReturnFlight().getFlightNumber() : null;

            for (Trip offer : currentOffers) {
                boolean matchOutbound = offer.getOutboundFlight().getFlightNumber().equals(targetOutboundNum);

                if (matchOutbound) {
                    if (savedTrip.isRoundTrip()) {
                        if (offer.isRoundTrip() && offer.getReturnFlight() != null &&
                                offer.getReturnFlight().getFlightNumber().equals(targetReturnNum)) {
                            return offer;
                        }
                    } else {
                        return offer;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao atualizar Trip " + savedTrip.getId() + ": " + e.getMessage());
            e.printStackTrace(); // Ajuda a ver o erro completo na consola
        }
        return null;
    }
    /**
     * Gera uma Trip simulada com preço mais baixo e horário alterado.
     */
    private Trip generateMockTripUpdate(Trip original) {
        System.out.println("[MOCK] A gerar atualização simulada para Trip: " + original.getId());

        // 1. Simular descida de preço (-50€)
        double newPrice = Math.max(10.0, original.getTotalPrice() - 50.0);

        // 2. Simular alteração de horário na Ida (+2 horas)
        Flight originalOutbound = original.getOutboundFlight();
        String newDepTime = originalOutbound.getDepartureTime();
        try {
            LocalDateTime dt = LocalDateTime.parse(originalOutbound.getDepartureTime());
            newDepTime = dt.plusHours(2).toString(); // Atraso de 2h
        } catch (Exception e) {}

        // Reconstruir Voo de Ida (Mock)
        // Nota: Para simplificar, reutilizamos os segmentos originais mas poderias alterá-los também
        Flight mockOutbound = new Flight(
                originalOutbound.getFlightNumber(),
                originalOutbound.getAirline(),
                originalOutbound.getOrigin(),
                originalOutbound.getDestination(),
                newDepTime, // Hora alterada
                originalOutbound.getArrivalTime(), // (Deveria ser ajustada também, mas serve para o teste)
                originalOutbound.getTotalDuration(),
                originalOutbound.getSegments()
        );

        // Reconstruir Voo de Volta (se existir)
        Flight mockReturn = null;
        if (original.isRoundTrip()) {
            mockReturn = original.getReturnFlight(); // Mantém igual neste teste
        }

        return new Trip(mockOutbound, mockReturn, newPrice);
    }

}