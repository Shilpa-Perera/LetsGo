package com.example.letsgo;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WifiScanner{

    private static final String TAG = "WifiScanner";
    private  WifiManager wifiManager;
    private  BroadcastReceiver wifiScanReceiver;
    private WifiScanListener wifiScanListener;
    private  Context context;
    private List<ScanResult> scanResults ;
    private List<AccessPoint> accessPoints;
    private Handler handler;
    private int totalDuration = 20000;
    private int scanInterval = 5000;
    private boolean isScanning = false;
    private int scanCount;


    public WifiScanner(Context context) {
        this.context = context;
        this.scanResults = new ArrayList<>();
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        handler = new Handler();
    }

    public WifiScanner (Context context , WifiScanListener scanListener){
        this.wifiScanListener = scanListener;
        this.context = context;
        this.scanResults = new ArrayList<>();
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        handler = new Handler();
    }
    private Runnable scanRunnable = new Runnable() {
        @Override
        public void run() {
            if(isScanning) {
                startWifiScan();
                handler.postDelayed(this, scanInterval);
            }
        }
    };

    private Runnable stopScanRunnable = new Runnable() {
        @Override
        public void run() {
            isScanning = false;
        }
    };

    public void startPeriodicScan() {

        if (!isScanning) {
            isScanning = true;
            handler.postDelayed(scanRunnable, scanInterval);
            handler.postDelayed(stopScanRunnable, totalDuration);
        }
    }

    private void startWifiScan() {
            wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                boolean success = intent.getBooleanExtra(
                        WifiManager.EXTRA_RESULTS_UPDATED, false);
                if (success) {
                    Log.d(TAG, "Scan results received");
                    scanSuccess();
                } else {
                    Log.d(TAG, "Scan results not received");
                    scanFailure();
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        context.registerReceiver(wifiScanReceiver, intentFilter);

        boolean success = wifiManager.startScan();
        if (!success) {
            scanFailure();
        }
    }
    private void scanSuccess() {
        @SuppressLint("MissingPermission") List<ScanResult> results = wifiManager.getScanResults();
        for (ScanResult result : results ){
            Log.d("Wifi", "BSSID: " + result.BSSID + ", RSSI: " + result.level);
        }
        scanResults.addAll(results);
        scanCount ++ ;
        if(scanCount == 3){
            scanResultsAnalyser();
        }
        context.unregisterReceiver(wifiScanReceiver);
    }

    private  void scanFailure() {
        if (wifiManager != null) {
            if (ActivityCompat.checkSelfPermission(context,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                List<ScanResult> results = wifiManager.getScanResults();
                Log.e(TAG, "Wi-Fi scan failed.");
            }

        }
    }

    private void scanResultsAnalyser(){

        Map<String, Integer[]> signalStrengths = new HashMap<>();

        for (ScanResult result : scanResults) {
            String bssId = result.BSSID;
            int strength = result.level;

            if (signalStrengths.containsKey(bssId)) {
                Integer[] data = signalStrengths.get(bssId);
                data[0] += strength;
                data[1]++;
            } else {
                signalStrengths.put(bssId, new Integer[]{strength, 1});
            }
        }

        // Calculate average signal strength for each BSSID
        accessPoints = new ArrayList<>();
        for (Map.Entry<String, Integer[]> entry : signalStrengths.entrySet()) {
            String bssId = entry.getKey();
            Integer[] data = entry.getValue();
            int averageStrength = data[0] / data[1];
            accessPoints.add(new AccessPoint(bssId, averageStrength));
        }

        for(AccessPoint ap : accessPoints){
            Log.d("WifiScanner" , "BSSID: " + ap.getBssId() + ", RSSI: " + ap.getStrength());
        }
    }

    public List<AccessPoint> getScanResults(){
        return accessPoints;
    }

    public void startNonPeriodicScan(){
        if (!isScanning) {
            isScanning = true;

            wifiScanReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context c, Intent intent) {
                    boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
                    if (success) {
                        @SuppressLint("MissingPermission")
                        List<ScanResult> results = wifiManager.getScanResults();
                        scanResults = results ;
                        wifiScanListener.onWifiScanReceived(scanResults);

                    } else {
                        Log.d(TAG, "Scan results not received");
                        scanFailure();
                    }

                    context.unregisterReceiver(this);
                    isScanning = false;
                }
            };

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            context.registerReceiver(wifiScanReceiver, intentFilter);

            // Start Wi-Fi scan
            boolean success = wifiManager.startScan();
            if (!success) {
                scanFailure();
                isScanning = false;
                context.unregisterReceiver(wifiScanReceiver);
            }

        }

    }

}


