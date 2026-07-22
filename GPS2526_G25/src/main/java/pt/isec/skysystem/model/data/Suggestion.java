package pt.isec.skysystem.model.data;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Suggestion {

    private final StringProperty destination;
    private final StringProperty tags;
    private final StringProperty reason;
    private final StringProperty priceRange;
    private final StringProperty imageUrl;

    public Suggestion(String destination, String tags, String reason, String priceRange, String imageUrl) {
        this.destination = new SimpleStringProperty(destination);
        this.tags = new SimpleStringProperty(tags);
        this.reason = new SimpleStringProperty(reason);
        this.priceRange = new SimpleStringProperty(priceRange);
        this.imageUrl = new SimpleStringProperty(imageUrl);
    }

    // --- Getters (Property) ---
    public StringProperty destinationProperty() {return destination;}
    public StringProperty tagsProperty() {return tags;}
    public StringProperty reasonProperty() {return reason;}
    public StringProperty priceRangeProperty() {return priceRange;}
    public StringProperty imageUrlProperty() {return imageUrl;}

    // --- Getters (Value) ---
    public String getDestination() {return destination.get();}
    public String getTags() {return tags.get();}
    public String getReason() {return reason.get();}
    public String getPriceRange() {return priceRange.get();}
    public String getImageUrl() {return imageUrl.get();}
}