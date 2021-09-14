package com.satoripop.intigo_demo.location;

public class LocationCoordinates {
    private double latitude;
    private double longitude;
    private long timestamp;

    public double getLatitude() {
        return latitude;
    }

    LocationCoordinates setLatitude(double latitude) {
        this.latitude = latitude;
        return this;
    }

    public double getLongitude() {
        return longitude;
    }

    LocationCoordinates setLongitude(double longitude) {
        this.longitude = longitude;
        return this;
    }

    public long getTimestamp() {
        return timestamp;
    }

    LocationCoordinates setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }
}