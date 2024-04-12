package com.example.letsgo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.Manifest;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.HashMap;

public class ReferencePointActivity extends AppCompatActivity {

    private ViewGroup rootView;
    private ImageView iconImageView;
    private Button button;
    private FirebaseFirestore firestore;
    private String mapDocId;
    private float imageViewWidth;
    private float imageViewHeight;

    private double latitude;
    private double longitude;
    private LocationRequest locationRequest;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reference_point);

        firestore = FirebaseFirestore.getInstance();

        ImageView imageView = findViewById(R.id.imageView);
        button = findViewById(R.id.nextButton);
        button.setEnabled(false);

        String imageUriString = getIntent().getStringExtra("imageUri");
        if (imageUriString != null) {
            Uri imageUri = Uri.parse(imageUriString);
            imageView.setImageURI(imageUri);
        }
        mapDocId = getIntent().getStringExtra("mapDocId");
        rootView = findViewById(android.R.id.content);

        /** Get Location Update */
        locationRequest = new LocationRequest.
                Builder(Priority.PRIORITY_HIGH_ACCURACY,2000).build();
        getCurrentLocation();

        imageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                imageViewWidth = imageView.getWidth();
                imageViewHeight = imageView.getHeight();
                imageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:


                        float x =  event.getX();
                        float y = event.getY() ;

                        int[] location = new int[2];
                        imageView.getLocationOnScreen(location);

                        int topX = location[0];
                        int topY = location[1];

                        displayIcon(x,y);
                        showPlaceNameDialog(x,y , topX , topY);
                }
                return true;
            }
        });

        button.setOnClickListener(view -> {

            System.out.println("image width, height : "+imageViewWidth+" , "+iconImageView );
            DocumentReference docRef = firestore.collection("map").document(mapDocId);
            docRef.update("mapHeight", imageViewHeight,
                    "mapWidth", imageViewWidth,
                    "gpsLatitude" , latitude,
                    "gpsLongitude" , longitude
            );
            Intent intent = new Intent(ReferencePointActivity.this, ShowMapActivity.class);
            intent.putExtra("mapDocId" , mapDocId);
            startActivity(intent);
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getCurrentLocation();

            }
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2) {
            if (resultCode == Activity.RESULT_OK) {

                getCurrentLocation();
            }
        }
    }

    private void showPlaceNameDialog ( float x, float y, int X, int Y){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Enter Reference Point Name");

            // Set up the input
            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            // Percentages of points
            float relativeX = (x - X) * 100 / imageViewWidth;
            float relativeY = (y - Y) * 100 / imageViewHeight;

            // Set up the buttons
            builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String locationName = input.getText().toString();
                    RefPoint refPoint = new RefPoint(mapDocId, locationName, relativeX, relativeY);
                    firestore.collection("ref_points")
                            .add(refPoint)
                            .addOnSuccessListener(documentReference -> {
                                Log.d("Firebase", "DocumentSnapshot written with ID: " + mapDocId);
                            })
                            .addOnFailureListener(e -> {
                                Log.e("Firebase", "Error adding document", e);
                            });
                    ;
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    if (iconImageView != null) {
                        rootView.removeView(iconImageView);
                    }
                }
            });

            builder.show();
        }

    private void displayIcon ( float x, float y){
            iconImageView = new ImageView(this);
            iconImageView.setImageResource(R.drawable.baseline_location_on_24); // Set your icon here
            int size = getResources().getDimensionPixelSize(R.dimen.icon_size); // Set icon size
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(size, size);
            params.leftMargin = (int) x - size / 2;
            params.topMargin = (int) y - size / 2;
            rootView.addView(iconImageView, params); // Add icon to root view
        }

    private void getCurrentLocation() {

        if (ActivityCompat.checkSelfPermission(
                ReferencePointActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {

            LocationServices.getFusedLocationProviderClient(ReferencePointActivity.this)
                        .requestLocationUpdates(locationRequest, new LocationCallback() {
                            @Override
                            public void onLocationResult(@NonNull LocationResult locationResult) {
                                super.onLocationResult(locationResult);

                                LocationServices.getFusedLocationProviderClient
                                                (ReferencePointActivity.this)
                                        .removeLocationUpdates(this);

                                if (!locationResult.getLocations().isEmpty()) {

                                    int index = locationResult.getLocations().size() - 1;
                                    latitude = locationResult.getLocations().get(index).getLatitude();
                                    longitude = locationResult.getLocations().get(index).getLongitude();
                                    button.setEnabled(true);
                                    Log.d("My" ,"Latitude: " + latitude + "\n" + "Longitude: " + longitude);
                                }
                            }
                        }, Looper.getMainLooper());


        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }
    }
