package com.example.letsgo;

import android.net.wifi.ScanResult;

import java.util.List;

public interface WifiScanListener {
    void onWifiScanReceived(List<ScanResult> scanResultList);
}
