package com.example.letsgo.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.Manifest;

import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


import com.example.letsgo.helpers.database.AccessPointManager;
import com.example.letsgo.R;
import com.example.letsgo.helpers.wifi.WifiScanner;
import com.example.letsgo.models.AccessPoint;
import com.example.letsgo.models.AccessPointInfo;
import com.example.letsgo.models.RefPoint;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firestore.v1.TargetOrBuilder;


import java.util.ArrayList;
import java.util.List;

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
    private WifiScanner wifiScanner;
    private AlertDialog dialog;
    private ProgressBar progressBar;
    private CountDownTimer countDownTimer;
    private TextView tvCountDown;


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

                        Log.d("Referencing Activity" , "AbsX : "+x);
                        Log.d("Referencing Activity", "AbsY : "+ y);

                        int[] location = new int[2];
                        imageView.getLocationOnScreen(location);

                        int topX = location[0];
                        int topY = location[1];

                        Log.d("Referencing Activity" , "topX : "+ topX);
                        Log.d("Referencing Activity" , "topY : "+topY);

                        displayIcon(x,y);
                        scanWifi();
                        showProgressBar(x,y , topX , topY);
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
            builder.setCancelable(false);


            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);



            float relativeX = (x - X) * 100 / imageViewWidth;
            float relativeY = (y - Y) * 100 / imageViewHeight;

            builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String locationName = input.getText().toString();
                    List<AccessPoint> accessPoints = wifiScanner.getScanResults();
                    ArrayList<AccessPointInfo> accessPointInfos = new ArrayList<>();
                    for(AccessPoint ap : accessPoints){
                        AccessPointInfo accessPointInfo = new AccessPointInfo();
                        accessPointInfo.setBssId(ap.getBssId());
                        accessPointInfo.setMapId(mapDocId);
                        accessPointInfos.add(accessPointInfo);
                    }
//                    AccessPointManager.getAccessPointFromDatabase(accessPointInfos ,mapDocId);
                    RefPoint refPoint = new RefPoint(mapDocId, locationName, relativeX, relativeY, accessPoints);
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

            builder.show();
        }

    private void displayIcon ( float x, float y){
            iconImageView = new ImageView(this);
            iconImageView.setImageResource(R.drawable.baseline_location_on_24);
            int size = getResources().getDimensionPixelSize(R.dimen.icon_size);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(size, size);
            params.leftMargin = (int) x - size / 2;
            params.topMargin = (int) y - size / 2;

            Log.d("Referencing Activity" , "leftMargin : "+ params.leftMargin);
            Log.d("Referencing Activity" , "rightMargin : "+ params.topMargin );

            rootView.addView(iconImageView, params);
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

    private void showProgressBar(float x,float y, int X, int Y){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.custom_dialog_layout, null);
        progressBar = dialogView.findViewById(R.id.progress_bar);
        tvCountDown = dialogView.findViewById(R.id.countdown_text);
        Button cancelButton = dialogView.findViewById(R.id.btnCancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissProgressBar();
                wifiScanner.stopPeriodicScan();
                if (iconImageView != null) {
                    rootView.removeView(iconImageView);
                }
            }
        });

        dialogView.setPadding(32, 32, 32, 32);

        builder.setView(dialogView);
        dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();

        startCountDownTimer(x,y,X,Y);
    }

    private void startCountDownTimer(float x, float y, int X, int Y) {
         countDownTimer = new CountDownTimer(17000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int progress = (int) (millisUntilFinished / 1000) * 100 / 17;
                progressBar.setProgress(progress);
                tvCountDown.setText("Please stay still: " + millisUntilFinished / 1000);
            }

            @Override
            public void onFinish() {
                dismissProgressBar();
                showPlaceNameDialog(x,y,X,Y);
            }
        }.start();
    }

    private void dismissProgressBar() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    private void scanWifi(){
        wifiScanner = new WifiScanner(this);
        wifiScanner.startPeriodicScan();
    }


}

