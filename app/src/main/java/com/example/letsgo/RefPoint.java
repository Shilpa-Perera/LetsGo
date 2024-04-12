package com.example.letsgo;

public class RefPoint {

    private String mapId;
    private String locationName;
    private float refPointX;
    private float refPointY;

    public RefPoint(String mapId, String locationName, float refPointX, float refPointY) {
        this.mapId = mapId;
        this.locationName = locationName;
        this.refPointX = refPointX;
        this.refPointY = refPointY;
    }

    public RefPoint() {
    }

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


}
