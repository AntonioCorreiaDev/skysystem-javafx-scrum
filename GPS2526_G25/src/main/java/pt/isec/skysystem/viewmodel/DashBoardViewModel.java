package pt.isec.skysystem.viewmodel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import pt.isec.skysystem.model.DataFacade;
import pt.isec.skysystem.model.data.Suggestion;

import java.util.Arrays;
import java.util.List;

public class DashBoardViewModel {
    private final DataFacade dataFacade;
    // Properties bound to the View
    private final StringProperty welcomeMessage;

    // Lists used by the View
    private final ObservableList<TripItem> favoriteTrips;
    private final ObservableList<RecommendationItem> recommendations;
    private final ObservableList<HistoryItem> history;

    public DashBoardViewModel() {
        this.dataFacade = DataFacade.getInstance();
        String nomeUtilizador = dataFacade.getNomeUtilizadorLogado();
        this.welcomeMessage = new SimpleStringProperty("WELCOME BACK, " + nomeUtilizador);
        this.favoriteTrips = FXCollections.observableArrayList();
        this.recommendations = FXCollections.observableArrayList();
        this.history = FXCollections.observableArrayList();

        // Load data immediately
        refresh();
    }

    public void refresh() {
        // Clear old data
        favoriteTrips.clear();
        recommendations.clear();
        history.clear();

        loadFavorites();
        loadRecommendations();
        loadHistory();
    }

    public boolean removeFavorite(String destinationName, String imgUrl) {
        if (dataFacade.removeFavoriteDestination(destinationName, imgUrl)) {
            refresh();
            return true;
        }
        return false;

    }

    // ============================================================================================
    // LOGIC: FAVORITES (Converting LinkedList<String> to Visual Cards)
    // ============================================================================================
    // ============================================================================================
    // LOGIC: FAVORITES (Converting LinkedList<String> to Visual Cards)
    // ============================================================================================
    // In DashBoardViewModel.java

    private void loadFavorites() {
        // 1. Get raw strings
        List<Suggestion> userFavorites = dataFacade.getFavoriteDestinations();

        // DEBUG: Adiciona isto para ver o que está a vir
        System.out.println("=== DEBUG: Favoritos carregados ===");
        System.out.println("Total de favoritos: " + userFavorites.size());

        if (userFavorites.isEmpty()) {
            System.out.println("⚠️ Nenhum favorito encontrado!");
            return;
        }

        // 2. Convert to cards
        for (Suggestion dest : userFavorites) {
            String destination = dest.getDestination();
            String imageUrl = dest.getImageUrl();

            // DEBUG: Print cada favorito
            System.out.println("Favorito: " + destination + " | ImageURL: " + imageUrl);

            // Se já temos URL, usa-a diretamente; senão, resolve pelo nome
            String imagePath = (imageUrl != null && !imageUrl.isEmpty())
                    ? imageUrl
                    : resolveImageForDestination(destination);

            System.out.println("ImagePath final: " + imagePath);

            favoriteTrips.add(new TripItem(destination, imagePath, dest.getReason(), dest.getPriceRange()));
        }

        System.out.println("Total de TripItems criados: " + favoriteTrips.size());
        System.out.println("===================================");
    }

    /**
     * Helper to find an image based on the destination name.
     */
    private String resolveImageForDestination(String destinationName) {
        if (destinationName == null || destinationName.isEmpty()) {
            return "/images/placeholders/default.jpg";
        }

        // Logic: Convert "New York" -> "/images/placeholders/new_york.jpg"
        String cleanName = destinationName.toLowerCase().replace(" ", "_");
        return "/images/placeholders/" + cleanName + ".jpg";
    }

    // ============================================================================================
    // LOGIC: OTHER SECTIONS
    // ============================================================================================
    private void loadRecommendations() {
        recommendations.add(new RecommendationItem("Bali, Indonesia", "/images/placeholders/bali.jpg"));
        recommendations.add(new RecommendationItem("Reykjavik, Iceland", "/images/placeholders/iceland.jpg"));
    }

    private void loadHistory() {
        history.add(new HistoryItem("Lisbon, Portugal", "Aug 2023", "/images/placeholders/lisbon.jpg"));
        history.add(new HistoryItem("London, UK", "Jan 2024", "/images/placeholders/london.jpg"));
    }

    // ============================================================================================
    // GETTERS & DATA CLASSES
    // ============================================================================================

    public StringProperty welcomeMessageProperty() {
        return welcomeMessage;
    }

    public ObservableList<TripItem> getFavoriteTrips() {
        return favoriteTrips;
    }

    public ObservableList<RecommendationItem> getRecommendations() {
        return recommendations;
    }

    public ObservableList<HistoryItem> getHistory() {
        return history;
    }

    // Simple Data Transfer Objects (DTOs)
    public static class TripItem {
        private final String name;
        private final String imagePath;
        private final String reason;
        private final String priceRange;

        public TripItem(String name, String imagePath, String reason, String priceRange) {
            this.name = name;
            this.imagePath = imagePath;
            this.reason = reason;
            this.priceRange = priceRange;
        }

        public String getName() {
            return name;
        }

        public String getImagePath() {
            return imagePath;
        }

        public String getReason() {
            return reason;
        }

        public String getPriceRange() {
            return priceRange;
        }
    }

    public static class RecommendationItem {
        private final String name;
        private final String imagePath;

        public RecommendationItem(String name, String imagePath) {
            this.name = name;
            this.imagePath = imagePath;
        }

        public String getName() {
            return name;
        }

        public String getImagePath() {
            return imagePath;
        }
    }

    public static class HistoryItem {
        private final String name;
        private final String date;
        private final String imagePath;

        public HistoryItem(String name, String date, String imagePath) {
            this.name = name;
            this.date = date;
            this.imagePath = imagePath;
        }

        public String getName() {
            return name;
        }

        public String getDate() {
            return date;
        }

        public String getImagePath() {
            return imagePath;
        }
    }
}