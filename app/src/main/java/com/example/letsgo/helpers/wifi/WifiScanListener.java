package com.example.letsgo.helpers.wifi;

import android.net.wifi.ScanResult;

import java.util.List;

public interface WifiScanListener {
    void onWifiScanReceived(List<ScanResult> scanResultList);
}
