package com.example.letsgo;

public class MapInfo {
    private String imageURI ;
    private int mapHeight ;
    private int mapWidth ;
    private String mapName ;

    private double gpsLatitude;
    private double gpsLongitude;

    public MapInfo(String imageURI, int mapHeight, int mapWidth, String mapName) {
        this.imageURI = imageURI;
        this.mapHeight = mapHeight;
        this.mapWidth = mapWidth;
        this.mapName = mapName;
    }

    public MapInfo() {
    }


    public String getImageURI() {
        return imageURI;
    }

    public void setImageURI(String imageURI) {
        this.imageURI = imageURI;
    }

    public int getMapHeight() {
        return mapHeight;
    }

    public void setMapHeight(int mapHeight) {
        this.mapHeight = mapHeight;
    }

    public int getMapWidth() {
        return mapWidth;
    }

    public void setMapWidth(int mapWidth) {
        this.mapWidth = mapWidth;
    }

    public String getMapName() {
        return mapName;
    }

    public void setMapName(String mapName) {
        this.mapName = mapName;
    }

    public double getGpsLatitude() {
        return gpsLatitude;
    }

    public void setGpsLatitude(double gpsLatitude) {
        this.gpsLatitude = gpsLatitude;
    }

    public double getGpsLongitude() {
        return gpsLongitude;
    }

    public void setGpsLongitude(double gpsLongitude) {
        this.gpsLongitude = gpsLongitude;
    }
}
