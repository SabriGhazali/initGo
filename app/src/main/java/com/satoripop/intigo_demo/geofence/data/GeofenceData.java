package com.satoripop.intigo_demo.geofence.data;

public class GeofenceData {

    String id ;
    double lat;
    double lng;
    double radius ;

    public GeofenceData(String id, double lat, double lng, double radius) {
        this.id = id;
        this.lat = lat;
        this.lng = lng;
        this.radius = radius;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }
}
