package pt.isec.skysystem.model.service;

public class LocationResult {
    public final String iataCode;
    public final String name;
    public final String detailedName;

    public LocationResult(String iataCode, String name, String subType, String cityName) {
        this.iataCode = iataCode;
        this.name = name;

        if ("CITY".equalsIgnoreCase(subType)) {
            this.detailedName = name + " (" + iataCode + ") - Cidade";
        } else {
            this.detailedName = name + " (" + iataCode + ") - Aeroporto";
        }
    }

    @Override
    public String toString() {
        return detailedName;
    }
}
