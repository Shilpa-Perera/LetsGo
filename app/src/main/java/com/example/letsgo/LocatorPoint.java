package com.example.letsgo;

public class LocatorPoint {
    private String mapId;
    private String locationName;
    private float refPointX;
    private float refPointY;
    private int distance;

    public String getMapId() {
        return mapId;
    }

    public void setMapId(String mapId) {
        this.mapId = mapId;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public float getRefPointX() {
        return refPointX;
    }

    public void setRefPointX(float refPointX) {
        this.refPointX = refPointX;
    }

    public float getRefPointY() {
        return refPointY;
    }

    public void setRefPointY(float refPointY) {
        this.refPointY = refPointY;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }
}
