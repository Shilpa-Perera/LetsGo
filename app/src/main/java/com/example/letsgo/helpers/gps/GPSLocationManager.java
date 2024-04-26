package com.example.letsgo.helpers.gps;



import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.example.letsgo.helpers.gps.GPSLocationListener;
import com.example.letsgo.models.GPSLocation;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class GPSLocationManager {
//    public interface GPSLocationListener {
//        void onLocationReceived(GPSLocation gpsLocation);
//    }
    private GPSLocationListener gpsLocationListener;


    public GPSLocationManager(GPSLocationListener gpsLocationListener ) {
        this.gpsLocationListener = gpsLocationListener;
    }

    public void getCurrentLocation(Context context , Activity activity ) {

        LocationRequest locationRequest = new LocationRequest.
                Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000).build();

        if (ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {

            LocationServices.getFusedLocationProviderClient(activity)
                    .requestLocationUpdates(locationRequest, new LocationCallback() {
                        @Override
                        public void onLocationResult(@NonNull LocationResult locationResult) {
                            super.onLocationResult(locationResult);

                            LocationServices.getFusedLocationProviderClient
                                            (activity)
                                    .removeLocationUpdates(this);

                            if (!locationResult.getLocations().isEmpty()) {

                                int index = locationResult.getLocations().size() - 1;
                                double latitude = locationResult.getLocations().get(index).getLatitude();
                                double longitude = locationResult.getLocations().get(index).getLongitude();
                                GPSLocation gpsLocation = new GPSLocation();
                                gpsLocation.setLatitude(latitude);
                                gpsLocation.setLongitude(longitude);
                                gpsLocationListener.onLocationReceived(gpsLocation);
                            }
                        }
                    }, Looper.getMainLooper());


        } else {
            activity.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

}
