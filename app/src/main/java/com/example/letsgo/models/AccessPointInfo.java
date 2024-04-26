package com.example.letsgo.models;
import com.google.firebase.firestore.PropertyName;



public class AccessPointInfo {

    @PropertyName("bssId")
    private String bssId;
    @PropertyName("mapId")
    private String mapId;

    public String getBssId() {
        return bssId;
    }

    public void setBssId(String bssId) {
        this.bssId = bssId;
    }

    public String getMapId() {
        return mapId;
    }

    public void setMapId(String mapId) {
        this.mapId = mapId;
    }
}
