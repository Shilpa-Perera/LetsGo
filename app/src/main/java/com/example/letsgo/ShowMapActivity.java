package com.example.letsgo;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

import java.sql.Ref;
import java.util.ArrayList;

public class ShowMapActivity extends AppCompatActivity {

    protected FirebaseFirestore firestore;

    protected StorageReference storageReference;
    private MapInfo mapInfo;
    private ArrayList<RefPoint> refPoints;

    private ImageView imageView;
    private ViewGroup rootView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_map);
        rootView = findViewById(android.R.id.content);
        String mapDocId = getIntent().getStringExtra("mapDocId");
        firestore = FirebaseFirestore.getInstance();
        DocumentReference docRef = firestore.collection("map").document(mapDocId);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                mapInfo = documentSnapshot.toObject(MapInfo.class);
                Log.d("GET FIREBASE DATA" , mapInfo.getMapName()+" , "+mapInfo
                        .getImageURI());

                imageView = findViewById(R.id.finalImage);

                storageReference = FirebaseStorage.getInstance().getReference(mapInfo.getImageURI());

                GlideApp.with(ShowMapActivity.this)
                        .load(storageReference)
                        .into(imageView);

                int[] location = new int[2];
                imageView.getLocationOnScreen(location);
                loadReferencePoints(mapDocId , location);

            }
        });
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

                                showRefPoint(document.toObject(RefPoint.class), locationX, locationY);
                                refPoints.add(document.toObject(RefPoint.class));
                                Log.d("Data", document.getId() + " => " + document.getData());
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


}
