package com.example.letsgo;

import android.location.Location;

import android.location.LocationListener;
import android.util.Log;



public class MyLocationListener implements LocationListener {

    @Override
    public void onLocationChanged(Location loc) {

        while(loc.getLongitude() == 0 && loc.getLatitude() == 0 ) {

            String longitude = "Longitude: " + loc.getLongitude();
            Log.v("Loc", longitude);
            String latitude = "Latitude: " + loc.getLatitude();
            Log.v("Loc", latitude);
        }

    }

}