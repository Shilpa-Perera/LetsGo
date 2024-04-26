package com.example.letsgo.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.letsgo.helpers.gps.GPSLocationManager;
import com.example.letsgo.helpers.gps.GPSLocationListener;
import com.example.letsgo.R;
import com.example.letsgo.helpers.Utils;
import com.example.letsgo.models.GPSLocation;
import com.example.letsgo.models.MapInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserShowMapsActivity extends AppCompatActivity implements GPSLocationListener {

    private GPSLocationManager gpsLocationManager;
    private GPSLocation currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_show_maps);

        gpsLocationManager = new GPSLocationManager(this);
        gpsLocationManager.getCurrentLocation(this,this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                gpsLocationManager.getCurrentLocation(this,this);
            }
        }
    }
    @Override
    public void onLocationReceived(GPSLocation gpsLocation) {
        currentLocation = new GPSLocation(gpsLocation.getLatitude(),gpsLocation.getLongitude());
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("map")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            ArrayList<MapInfo> allMaps = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                MapInfo mapInfo = document.toObject(MapInfo.class);
                                String docId = document.getId();
                                mapInfo.setMapDocId(docId);
                                allMaps.add(mapInfo);
                                Log.d("APFirebase", mapInfo.getMapDocId());
                            }

                            List<MapInfo> mapInfoList =
                                    Utils.filterCoordinatesByRadius(allMaps
                                            ,gpsLocation.getLatitude()
                                    ,gpsLocation.getLongitude()
                                    ,100);

                            showFilteredMaps(mapInfoList);
                            for (MapInfo mapInfo : mapInfoList){
                                Log.d("Filtered Maps" , mapInfo.getMapName());
                            }

                        } else {
                            Log.d("APFirebase", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void showFilteredMaps(List<MapInfo> mapInfoList) {
        ListView listView = findViewById(R.id.listView);

        // Create Adapter
        ArrayAdapter<MapInfo> adapter = new ArrayAdapter<MapInfo>(
                this,
                android.R.layout.simple_list_item_1,
                mapInfoList
                );

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MapInfo selectedMap = mapInfoList.get(position);
                Intent intent = new Intent(UserShowMapsActivity.this, ShowMapActivity.class);
                intent.putExtra("mapDocId", selectedMap.getMapDocId());
                startActivity(intent);
            }
        });
    }

}
