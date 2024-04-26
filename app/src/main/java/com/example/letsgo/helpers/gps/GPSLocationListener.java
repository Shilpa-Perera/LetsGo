package com.example.letsgo.helpers.gps;

import com.example.letsgo.models.GPSLocation;

public interface GPSLocationListener {
    void onLocationReceived(GPSLocation gpsLocation);

}
