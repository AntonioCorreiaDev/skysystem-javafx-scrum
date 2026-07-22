package pt.isec.skysystem.model.data;

import pt.isec.skysystem.model.data.flights.Trip;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * User profile containing username and travel preferences
 */
public class UserProfile {
    private String username;
    private String email;
    private TravelPreferences preferences;
    private boolean onboardingCompleted;
    private List<Suggestion> FavoriteDestinations;
    private ArrayList<Trip> savedFlights;

    public List<Suggestion> getFavoriteDestinations() {
        return FavoriteDestinations;
    }

    public void setSavedSuggetions(List<Suggestion> savedSuggetions) {
        this.FavoriteDestinations = savedSuggetions;
    }

    public void addFavoriteDestination(String destination, String imgUrl, String tags, String reason,
            String priceRange) {
        if (this.FavoriteDestinations == null) {
            this.FavoriteDestinations = new java.util.LinkedList<>();
        }

        boolean exists = this.FavoriteDestinations.stream()
                .anyMatch(s -> s.getDestination().equalsIgnoreCase(destination));

        if (!exists) {
            Suggestion suggestion = new Suggestion(destination, tags, reason, priceRange, imgUrl);
            this.FavoriteDestinations.add(suggestion);
        }
    }

    public List<Suggestion> getFavouriteDestinations() {
        return FavoriteDestinations;
    }

    public boolean removeFavoriteDestination(String destination, String imgUrl) {
        if (FavoriteDestinations != null) {
            Suggestion suggestion = new Suggestion(destination, "", "", "", imgUrl);

            if (FavoriteDestinations
                    .removeIf(suggestion1 -> suggestion1.getDestination().equalsIgnoreCase(destination))) {

                FavoriteDestinations.remove(suggestion);
                return true;
            }
        }
        return false;
    }

    public UserProfile(String username, String email, boolean onboardingCompleted) {
        this.email = email;
        this.username = username;
        this.FavoriteDestinations = new LinkedList<>();
        this.preferences = new TravelPreferences();
        this.onboardingCompleted = onboardingCompleted;
        this.savedFlights = new ArrayList<>();
    }

    // GETTERS
    public String getUsername() {
        return username;
    }

    public TravelPreferences getPreferences() {
        return preferences;
    }

    public boolean isOnboardingCompleted() {
        return onboardingCompleted;
    }

    public String getEmail() {
        return email;
    }

    // SETTERS
    public void setUsername(String username) {
        this.username = username;
    }

    public void setPreferences(TravelPreferences preferences) {
        this.preferences = preferences;
    }

    public void setOnboardingCompleted(boolean completed) {
        this.onboardingCompleted = completed;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // CONVENIENCE METHODS
    public void completeOnboarding(TravelPreferences preferences) {
        this.preferences = preferences;
        this.onboardingCompleted = true;
    }

    public boolean needsOnboarding() {
        return !onboardingCompleted || !preferences.isComplete();
    }

    /**
     * Get budget range as min-max for backward compatibility
     */
    public int getMinBudget() {
        if (preferences.getBudgetLevel() != null) {
            return preferences.getBudgetLevel().getMinDaily();
        }
        return 0;
    }

    public int getMaxBudget() {
        if (preferences.getBudgetLevel() != null) {
            return preferences.getBudgetLevel().getMaxDaily();
        }
        return Integer.MAX_VALUE;
    }

    @Override
    public String toString() {
        return "UserProfile{" +
                "username='" + username + '\'' +
                ", preferences=" + preferences +
                '}';
    }

    // === SAVED FLIGHTS MANAGEMENT ===
    public ArrayList<Trip> getSavedFlights() {
        if (savedFlights == null)
            savedFlights = new ArrayList<>();
        return savedFlights;
    }

    public void addSavedFlight(Trip trip) {
        if (savedFlights == null)
            savedFlights = new ArrayList<>();
        if (!savedFlights.contains(trip)) {
            savedFlights.add(trip);
        }
    }

    public void setSavedFlights(List<Trip> savedFlights) {
        this.savedFlights = (ArrayList<Trip>) savedFlights;
    }

    public void removeSavedFlight(Trip trip) {
        if (savedFlights != null) {
            savedFlights.remove(trip);
        }
    }

    public boolean isFlightSaved(String outgoingFlightNumber, String returnFlightNumber) {
        if (savedFlights == null)
            return false;
        if (returnFlightNumber != null) {
            return savedFlights.stream()
                    .anyMatch(t -> t.getOutboundFlight().getFlightNumber().equals(outgoingFlightNumber) &&
                            t.getReturnFlight() != null &&
                            t.getReturnFlight().getFlightNumber() != null &&
                            t.getReturnFlight().getFlightNumber().equals(returnFlightNumber));
        }
        return savedFlights.stream()
                .anyMatch(t -> t.getOutboundFlight().getFlightNumber().equals(outgoingFlightNumber) &&
                        (t.getReturnFlight() == null || t.getReturnFlight().getFlightNumber() == null));
    }

}