package com.example.letsgo.models;

import java.util.List;

public class RefPoint {

    private String mapId;
    private String locationName;
    private float refPointX;
    private float refPointY;

    private List<AccessPoint> accessPointList;

    public RefPoint(String mapId, String locationName, float refPointX, float refPointY,
                    List<AccessPoint> accessPointList) {
        this.mapId = mapId;
        this.locationName = locationName;
        this.refPointX = refPointX;
        this.refPointY = refPointY;
        this.accessPointList = accessPointList;
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

    public List<AccessPoint> getAccessPointList() {
        return accessPointList;
    }
    public void setAccessPointList(List<AccessPoint> accessPointList) {
        this.accessPointList = accessPointList;
    }
}
