package com.example.letsgo.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.letsgo.GlideApp;
import com.example.letsgo.R;
import com.example.letsgo.helpers.wifi.WifiScanListener;
import com.example.letsgo.helpers.wifi.WifiScanner;
import com.example.letsgo.models.AccessPoint;
import com.example.letsgo.models.LocatorPoint;
import com.example.letsgo.models.MapInfo;
import com.example.letsgo.models.RefPoint;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShowMapActivity extends AppCompatActivity implements WifiScanListener{

    protected FirebaseFirestore firestore;
    protected StorageReference storageReference;
    private MapInfo mapInfo;
    private WifiScanner wifiScanner;
    private Handler handler;
    private ArrayList<RefPoint> refPoints;
    private Map<String , Integer> accessPoints;
    private ImageView imageView;
    private ImageView icon;
    private ViewGroup rootView;

    @SuppressLint("MissingInflatedId")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_map);
        rootView = findViewById(android.R.id.content);
        String mapDocId = getIntent().getStringExtra("mapDocId");
        firestore = FirebaseFirestore.getInstance();
        DocumentReference docRef = firestore.collection("map").document(mapDocId);
        wifiScanner = new WifiScanner(this, this);
        handler = new Handler();


        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                mapInfo = documentSnapshot.toObject(MapInfo.class);
                Log.d("Map Data" , mapInfo.getMapName()+" , "+mapInfo
                        .getImageURI());

                imageView = findViewById(R.id.finalImage);

                storageReference = FirebaseStorage.getInstance().getReference(mapInfo.getImageURI());

                GlideApp.with(ShowMapActivity.this)
                        .load(storageReference)
                        .into(imageView);

                int[] location = new int[2];
                imageView.getLocationOnScreen(location);
                loadReferencePoints(mapDocId , location);

                getLocationUpdate();

            }
        });

    }



        private void getLocationUpdate(){
            long delayMillis = 5000;
            Runnable scanRunnable = new Runnable() {
                @Override
                public void run() {
                    if(icon != null){
                        rootView.removeView(icon);
                    }
                    wifiScanner.startNonPeriodicScan();
                    handler.postDelayed(this, delayMillis);
                }
            };

            handler.postDelayed(scanRunnable, delayMillis);
    }

    private void loadReferencePoints(String mapDocId, int[] location ){
        int locationX = location[0];
        int locationY = location[1];

        firestore.collection("ref_points")
                .whereEqualTo("mapId", mapDocId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            refPoints = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {

//                                showRefPoint(document.toObject(RefPoint.class), locationX, locationY);
                                refPoints.add(document.toObject(RefPoint.class));
                            }
                        } else {
                            Log.d("Data", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void showRefPoint(RefPoint ref , int locationX , int locationY){

        float pointX = ref.getRefPointX() * imageView.getWidth() / 100 ;
        float pointY = ref.getRefPointY() * imageView.getHeight() / 100 ;

        int absoluteX = (int) (locationX + pointX ) ;
        int absoluteY = (int) (locationY + pointY ) ;

        ImageView icon = new ImageView(this);

        icon.setImageResource(R.drawable.baseline_location_on_24);
        int size = getResources().getDimensionPixelSize(R.dimen.icon_size);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(size,size);

        params.leftMargin = absoluteX - size/2;
        params.topMargin = absoluteY - size/2 ;

        rootView.addView(icon, params);
    }

    private void showLocationPoint(float x  , float y){

        int[] location = new int[2];
        imageView.getLocationOnScreen(location);

        int absoluteX = (int) (x ) ;
        int absoluteY = (int) ( y ) ;

        icon = new ImageView(this);

        icon.setImageResource(R.drawable.location);
        int size = getResources().getDimensionPixelSize(R.dimen.locator_icon_size);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(size,size);

        params.leftMargin = absoluteX - size/2;
        params.topMargin = absoluteY - size/2 ;

        Log.d("Final" , params.leftMargin+" ");
        Log.d("Final", params.topMargin + " ");
//
//        params.leftMargin = (int) x - size/2;
//        params.topMargin = (int)  y - size/2 ;

        rootView.addView(icon, params);
    }

    @Override
    public void onWifiScanReceived(List<ScanResult> scanResultList) {

        List<ScanResult> filteredRessults = new ArrayList<>();

        for(ScanResult scanResult : scanResultList){
            if (scanResult.SSID.equals("UoM_Wireless") || scanResult.SSID.equals("eduroam")) {
                filteredRessults.add(scanResult);
            }

        }
        filteredRessults.sort(new Comparator<ScanResult>() {
            @Override
            public int compare(ScanResult o1, ScanResult o2) {
                return Integer.compare(o2.level, o1.level);
            }
        });

        //TODO: REMOVING TAKING TOP FIVE
//        List<ScanResult> topFiveStrongest = filteredRessults.subList(0, Math.min(5, filteredRessults.size()));

        accessPoints = new HashMap<>();
        for(ScanResult scanResult : filteredRessults){
            accessPoints.put(scanResult.BSSID , scanResult.level);
        }

        ArrayList<LocatorPoint> locatorPoints = new ArrayList<>();
        for (RefPoint refPoint : refPoints){
                int distance = calculateEuclideanDistance(accessPoints , refPoint.getAccessPointList());
                LocatorPoint locatorPoint = new LocatorPoint();
                locatorPoint.setDistance(distance);
                locatorPoint.setRefPointX(refPoint.getRefPointX());
                locatorPoint.setRefPointY(refPoint.getRefPointY());
                locatorPoints.add(locatorPoint);
        }

        locatorPoints.sort(Comparator.comparingInt(LocatorPoint::getDistance));
        calculateWeightedAverage(locatorPoints);
    }

    private void calculateWeightedAverage(List<LocatorPoint> locatorPoints){
        int k = 3;
        int locatorPointsSize = locatorPoints.size();
        
        float weightedSumX = 0;
        float weightedSumY = 0;
        float sumWeight = 0;
        float locationWeight;

        List<LocatorPoint> kLocatorPoints =
                locatorPoints.subList(0 , Math.min(k, locatorPointsSize));

        int[] location = new int[2];
        imageView.getLocationOnScreen(location);
        int topX = location[0];
        int topY = location[1];

        for (LocatorPoint locatorPoint : kLocatorPoints){
            if (locatorPoint.getDistance() != 0 ){
                locationWeight = (float) 1 / locatorPoint.getDistance();
            }
            else {
                locationWeight = 100 ;
            }
            
//            float x_cordinate = (locatorPoint.getRefPointX()) * imageView.getWidth() / 100;
//            float y_cordinate = (locatorPoint.getRefPointY()) * imageView.getHeight() / 100;

            float x_cordinate = locatorPoint.getRefPointX();
            float y_cordinate = locatorPoint.getRefPointY();

            Log.d("Locator Point" , x_cordinate + " , "+ y_cordinate);
            Log.d("Locator Point" , "locationWeight : "+ locationWeight);
            
            sumWeight += locationWeight ;
            weightedSumX += locationWeight * x_cordinate ;
            weightedSumY += locationWeight * y_cordinate ;

        }

        weightedSumX /= sumWeight;
        weightedSumY /= sumWeight;

        Log.d("MapShow" , "topX : "+ topX);
        Log.d("MapShow" , "topY : "+ topY );

        Log.d("MapShow" , "weightedSumX : "+ weightedSumX);
        Log.d("MapShow" , "weightedSumY : "+ weightedSumY);

        showLocationPoint(weightedSumX , weightedSumY);
    }

    private int calculateEuclideanDistance(Map<String , Integer> currentAPMap ,
                                            List<AccessPoint> refPointAPList ){

        for (Map.Entry<String, Integer> entry : currentAPMap.entrySet()) {
            Log.d("CurrentAP","Key: " + entry.getKey() + ", Value: " + entry.getValue());
        }

        Map<String , Integer> refPointAPMap = new HashMap<>();
        for (AccessPoint accessPoint : refPointAPList){
            refPointAPMap.put(accessPoint.getBssId() , accessPoint.getStrength());
        }

        for (Map.Entry<String, Integer> entry : refPointAPMap.entrySet()) {
            Log.d("RefPointAP","Key: " + entry.getKey() + ", Value: " + entry.getValue());
        }

        int total_distance = 0 ;
        for(Map.Entry<String , Integer> bssId : currentAPMap.entrySet()){
            if (refPointAPMap.containsKey(bssId.getKey())){
                int distance_1 = bssId.getValue();
                int distance_2 = refPointAPMap.get(bssId.getKey());

                int temp_distance = distance_1 - distance_2 ;
                int distance = temp_distance * temp_distance;

//                int distance1 = (int) Math.sqrt(distance);

                total_distance += distance ;
            }
        }

        return total_distance;
    }

    private int calculateManhattanDistance(Map<String, Integer> currentAPMap, List<AccessPoint> refPointAPList) {
        for (Map.Entry<String, Integer> entry : currentAPMap.entrySet()) {
            Log.d("CurrentAP", "Key: " + entry.getKey() + ", Value: " + entry.getValue());
        }

        Map<String, Integer> refPointAPMap = new HashMap<>();
        for (AccessPoint accessPoint : refPointAPList) {
            refPointAPMap.put(accessPoint.getBssId(), accessPoint.getStrength());
        }

        for (Map.Entry<String, Integer> entry : refPointAPMap.entrySet()) {
            Log.d("RefPointAP", "Key: " + entry.getKey() + ", Value: " + entry.getValue());
        }

        int totalDistance = 0;
        for (Map.Entry<String, Integer> bssId : currentAPMap.entrySet()) {
            if (refPointAPMap.containsKey(bssId.getKey())) {
                int distance1 = bssId.getValue();
                int distance2 = refPointAPMap.get(bssId.getKey());

                int tempDistance = Math.abs(distance1 - distance2);

                totalDistance += tempDistance;
            }
        }

        return totalDistance;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

}
