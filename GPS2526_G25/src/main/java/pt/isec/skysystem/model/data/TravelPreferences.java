package pt.isec.skysystem.model.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates all travel preferences from the onboarding quiz
 * Contains all 5 questions as inner enums
 */
public class TravelPreferences {

    // Question 1: Interests/Vibe (multi-select, max 3)
    private Vibe vibe;

    // Question 2: Budget
    private BudgetLevel budgetLevel;

    // Question 3: Pace
    private TravelPace pace;

    // Question 4: Company
    private TravelCompany company;

    // Question 5: Climate
    private ClimatePreference climate;

    // Constructor
    public TravelPreferences() {}

    // ===== GETTERS =====

    public Vibe getVibe() {
        return vibe;
    }

    public BudgetLevel getBudgetLevel() {
        return budgetLevel;
    }

    public TravelPace getPace() {
        return pace;
    }

    public TravelCompany getCompany() {
        return company;
    }

    public ClimatePreference getClimate() {
        return climate;
    }

    // ===== SETTERS =====

    public void setVibe(Vibe vibe) {
        this.vibe = vibe;
    }

    public void setBudgetLevel(BudgetLevel budgetLevel) {
        this.budgetLevel = budgetLevel;
    }

    public void setPace(TravelPace pace) {
        this.pace = pace;
    }

    public void setCompany(TravelCompany company) {
        this.company = company;
    }

    public void setClimate(ClimatePreference climate) {
        this.climate = climate;
    }

    // ===== VALIDATION =====

    /**
     * Check if all quiz questions are answered
     */
    public boolean isComplete() {
        return vibe != null &&
                budgetLevel != null &&
                pace != null &&
                company != null &&
                climate != null;
    }

    /**
     * Get all tags for recommendation matching
     */
    public List<String> getAllTags() {
        List<String> tags = new ArrayList<>();

        // Add vibe tags
        if (vibe != null) {
            tags.addAll(List.of(vibe.getTags()));
        }

        // Add budget tag
        if (budgetLevel != null) {
            tags.add(budgetLevel.getTag());
        }

        // Add pace tag
        if (pace != null) {
            tags.add(pace.getTag());
        }

        // Add company tags
        if (company != null) {
            tags.addAll(List.of(company.getTags()));
        }

        // Add climate tags
        if (climate != null) {
            tags.addAll(List.of(climate.getTags()));
        }

        return tags;
    }

    // ENUMS
    public enum Vibe {
        // Vibe/Interests (Question 1)
        BEACH_SUN("Beach & Sun", "beach", "relaxing"),
        MOUNTAIN_ADVENTURE("Mountain & Adventure", "mountain", "adventure", "hiking"),
        CITY_CULTURE("City & Culture", "city", "museums", "culture"),
        HISTORY_LANDMARKS("History & Landmarks", "history", "landmarks"),
        NATURE_WILDLIFE("Nature & Wildlife", "nature", "wildlife", "outdoors"),
        FOOD_WINE("Food & Wine", "foodie", "culinary");

        private final String displayName;
        private final String[] tags;

        Vibe(String displayName, String... tags) {
            this.displayName = displayName;
            this.tags = tags;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String[] getTags() {
            return tags;
        }

        public String toUI() {
            return switch (this) {
                case MOUNTAIN_ADVENTURE -> "Mountain";
                case CITY_CULTURE -> "City";
                case BEACH_SUN -> "Beach";
                case NATURE_WILDLIFE -> "CountrySide";
                default -> "";
            };
        }

    }

    public enum BudgetLevel {
        LOW_END("Low-End", 0, 30, "budget_low"),
        MID_RANGE("Mid-Range", 30, 100, "budget_medium"),
        HIGH_END("High-End", 100, 250, "budget_high"),
        LUXURY("Luxury", 250, Integer.MAX_VALUE, "budget_luxury");

        private final String displayName;
        private final int minDaily;
        private final int maxDaily;
        private final String tag;

        BudgetLevel(String displayName, int minDaily, int maxDaily, String tag) {
            this.displayName = displayName;
            this.minDaily = minDaily;
            this.maxDaily = maxDaily;
            this.tag = tag;
        }

        public static BudgetLevel fromDaily(double dailyBudget) {
            for (BudgetLevel level : BudgetLevel.values()) {
                if (level.matches((int) dailyBudget)) {
                    return level;
                }
            }
            return MID_RANGE; // fallback seguro
        }



        public String getDisplayName() { return displayName; }
        public int getMinDaily() { return minDaily; }
        public int getMaxDaily() { return maxDaily; }
        public String getTag() { return tag; }

        public String getDescription() {
            if (maxDaily == Integer.MAX_VALUE) {
                return String.format("$%d+ per day", minDaily);
            }
            return String.format("$%d-$%d per day", minDaily, maxDaily);
        }
        public boolean matches(int dailyBudget) {
            return dailyBudget >= minDaily && dailyBudget < maxDaily;
        }
    }

    public enum TravelPace {
        ACTION_PACKED("Action-Packed", "style_fast_pace"),
        BALANCED("A Bit of Both", "style_balanced"),
        RELAXED("Slow & Relaxing", "style_relaxed");

        private final String displayName;
        private final String tag;

        TravelPace(String displayName, String tag) {
            this.displayName = displayName;
            this.tag = tag;
        }

        public String getDisplayName() { return displayName; }
        public String getTag() { return tag; }

        public String toUI() {
            return switch (this) {
                case RELAXED -> "Relaxed";
                case BALANCED -> "Moderate";
                case ACTION_PACKED -> "High";
            };
        }

    }

    public enum TravelCompany {
        SOLO("Solo", "solo"),
        PARTNER("Partner", "couples", "romantic"),
        FRIENDS("Friends", "groups", "nightlife"),
        FAMILY("Family", "family_friendly");

        private final String displayName;
        private final String[] tags;

        TravelCompany(String displayName, String... tags) {
            this.displayName = displayName;
            this.tags = tags;
        }

        public String getDisplayName() { return displayName; }
        public String[] getTags() { return tags; }

        public String toUI() {
            return switch (this) {
                case SOLO -> "Solo";
                case FRIENDS -> "Friends";
                case PARTNER -> "Couple";
                case FAMILY -> "Family";
            };
        }

    }

    public enum ClimatePreference {
        HOT_SUNNY("Hot & Sunny", "climate_hot", "climate_tropical"),
        WARM_MILD("Warm & Mild", "climate_temperate"),
        COOL_CRISP("Cool & Crisp", "climate_cool", "climate_cold"),
        NO_PREFERENCE("Doesn't Matter!", "climate_any");

        private final String displayName;
        private final String[] tags;

        ClimatePreference(String displayName, String... tags) {
            this.displayName = displayName;
            this.tags = tags;
        }

        public String getDisplayName() { return displayName; }
        public String[] getTags() { return tags; }

        public String toUI() {
            return switch (this) {
                case HOT_SUNNY -> "Hot";
                case WARM_MILD -> "Warm";
                case COOL_CRISP -> "Snowy";
                case NO_PREFERENCE -> "No Preference";
            };
        }

    }





    @Override
    public String toString() {
        return "TravelPreferences{" +
                "vibe=" + vibe +
                ", budgetLevel=" + budgetLevel +
                ", pace=" + pace +
                ", company=" + company +
                ", climate=" + climate +
                '}';
    }
}