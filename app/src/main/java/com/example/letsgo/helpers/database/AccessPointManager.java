package com.example.letsgo.helpers.database;


import android.util.Log;

import androidx.annotation.NonNull;

import com.example.letsgo.models.AccessPointInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class AccessPointManager {

    private static final String COLLECTION_NAME = "access_points";

    public AccessPointManager(){

    }

    public static void getAccessPointFromDatabase(ArrayList<AccessPointInfo> currentList, String mapId){
        Log.d("Here" , "I am here");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(COLLECTION_NAME)
                .whereEqualTo("mapId", mapId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            ArrayList<AccessPointInfo> arrayListFromDb = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                AccessPointInfo accessPointInfo = document.toObject(AccessPointInfo.class);
                                arrayListFromDb.add(accessPointInfo);
                                Log.d("APFirebase", accessPointInfo.getBssId()+" , "
                                        + accessPointInfo.getMapId());
                            }
                            compareData(arrayListFromDb , currentList);
                        } else {
                            Log.d("APFirebase", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    public static void compareData(ArrayList<AccessPointInfo> arrayListFromDb ,
                             ArrayList<AccessPointInfo> currentArrayList){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        ArrayList<AccessPointInfo> storingArr = new ArrayList<>();
        for (AccessPointInfo currentAP : currentArrayList) {
            boolean found = false;
            for (AccessPointInfo dbAP : arrayListFromDb) {
                if (currentAP.getBssId().equals(dbAP.getBssId())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                db.collection("access_points")
                        .add(currentAP)
                        .addOnSuccessListener(documentReference -> {
                            Log.d("Firebase", "DocumentSnapshot written with ID: ");
                        })
                        .addOnFailureListener(e -> {
                            Log.e("Firebase", "Error adding document", e);
                        });
                storingArr.add(currentAP);
            }
        }

    }

}