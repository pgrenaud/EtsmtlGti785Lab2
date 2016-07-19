package ca.etsmtl.gti785.lib.entity;

import android.location.Location;

public class LocationEntity {
    private Double latitude;
    private Double longitude;

    public LocationEntity() {
        this(0.0, 0.0);
    }

    public LocationEntity(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public float distanceTo(LocationEntity dest) {
        return distanceBetween(latitude, longitude, dest.getLatitude(), dest.getLongitude());
    }

    public float distanceTo(Location dest) {
        return distanceBetween(latitude, longitude, dest.getLatitude(), dest.getLongitude());
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public static float distanceBetween(double startLatitude, double startLongitude, double endLatitude, double endLongitude) {
        float[] results = new float[1];

        Location.distanceBetween(startLatitude, startLongitude, endLatitude, endLongitude, results);

        return results[0];
    }
}
