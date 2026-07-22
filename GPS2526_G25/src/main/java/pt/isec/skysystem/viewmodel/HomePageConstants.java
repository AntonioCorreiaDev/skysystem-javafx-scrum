package pt.isec.skysystem.viewmodel;

import java.util.Arrays;
import java.util.List;

/**
 * Constants for the HomePageViewModel
 * Contains default slideshow images and configuration
 */
public class HomePageConstants {

    // Slideshow image dimensions
    public static final int SLIDESHOW_IMAGE_WIDTH = 1200;
    public static final int SLIDESHOW_IMAGE_HEIGHT = 800;

    // Default slideshow images from Unsplash
    public static final List<String> DEFAULT_SLIDESHOW_IMAGES = Arrays.asList(
            "https://images.unsplash.com/photo-1537996194471-e657df975ab4?auto=format&fit=crop&w=1200&h=800&q=80",
            "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?auto=format&fit=crop&w=1200&h=800&q=80",
            "https://images.unsplash.com/photo-1513635269975-59663e0ac1ad?auto=format&fit=crop&w=1200&h=800&q=80",
            "https://images.unsplash.com/photo-1476610182048-b716b8518aae?auto=format&fit=crop&w=1200&h=800&q=80",
            "https://images.unsplash.com/photo-1496442226666-8d4d0e62e6e9?auto=format&fit=crop&w=1200&h=800&q=80", // New York
            "https://images.unsplash.com/photo-1530122037265-a5f1f91d3b99?auto=format&fit=crop&w=1200&h=800&q=80", // Swiss Alps
            "https://images.unsplash.com/photo-1552832230-c0197dd311b5?auto=format&fit=crop&w=1200&h=800&q=80", // Rome (Colosseum)
            "https://images.unsplash.com/photo-1506973035872-a4ec16b8e8d9?auto=format&fit=crop&w=1200&h=800&q=80", // Sydney (Opera House)
            "https://images.unsplash.com/photo-1587595431973-160d0d94add1?auto=format&fit=crop&w=1200&h=800&q=80", // Machu Picchu
            "https://images.unsplash.com/photo-1528181304800-259b08848526?auto=format&fit=crop&w=1200&h=800&q=80" // Thailand
    );

    // Generic scenic search terms
    public static final List<String> SCENIC_SEARCH_TERMS = Arrays.asList("scenery","view");

    private HomePageConstants() {}
}
