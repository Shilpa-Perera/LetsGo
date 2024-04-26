package com.example.letsgo;

public class AccessPoint {

    private String ssId;
    private String bssId ;
    private int strength;

    public AccessPoint(String bssId, int strength) {
        this.bssId = bssId;
        this.strength = strength;
    }


    public AccessPoint() {
    }

    public String getBssId() {
        return bssId;
    }

    public void setBssId(String bssId) {
        this.bssId = bssId;
    }

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }
}
