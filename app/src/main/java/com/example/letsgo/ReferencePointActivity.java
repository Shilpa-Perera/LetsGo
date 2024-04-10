package com.example.letsgo;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.github.chrisbanes.photoview.PhotoView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firestore.v1.WriteResult;

import java.util.HashMap;

public class ReferencePointActivity extends AppCompatActivity {

//    private ImageView imageView;
    private EditText placeNameEditText;
    private ViewGroup rootView;
    private  ImageView iconImageView ;

    private FirebaseFirestore firestore;
    private String mapDocId;
    private  int imageViewWidth;
    private  int imageViewHeight;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reference_point);

        ImageView imageView = findViewById(R.id.imageView);
        String imageUriString = getIntent().getStringExtra("imageUri");
        if (imageUriString != null) {
            Uri imageUri = Uri.parse(imageUriString);
            imageView.setImageURI(imageUri);
        }
        mapDocId = getIntent().getStringExtra("mapDocId");
        placeNameEditText = findViewById(R.id.placeNameEditText);
        rootView = findViewById(android.R.id.content);

        // Add global layout listener to get dimensions after layout is drawn
        imageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Get the dimensions of the ImageView
                imageViewWidth = imageView.getWidth();
                imageViewHeight = imageView.getHeight();

                // Update an existing document
                firestore = FirebaseFirestore.getInstance();
                DocumentReference docRef = firestore.collection("map").document(mapDocId);
                docRef.update("mapHeight", imageViewHeight, "mapWidth", imageViewWidth);

                // Remove the listener to avoid multiple calls
                imageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        float x = event.getX();
                        float y = event.getY();

                        displayIcon(x,y);
                        showPlaceNameDialog(x,y);
                }
                return true;
            }
        });

    }

    private void showPlaceNameDialog(float x, float y) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Reference Point Name");

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String locationName = input.getText().toString();
                float normalizedX = x / imageViewWidth;
                float normalizedY = y / imageViewHeight;
                RefPoint refPoint = new RefPoint(mapDocId,locationName,normalizedX,normalizedY);
                firestore.collection("ref_points")
                        .add(refPoint)
                        .addOnSuccessListener(documentReference -> {
                            Log.d("Firebase", "DocumentSnapshot written with ID: " + mapDocId);
                        })
                        .addOnFailureListener(e -> {
                            Log.e("Firebase", "Error adding document", e);
                        });;
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

    private void displayIcon(float x, float y) {
        // Create a new ImageView for the icon
        iconImageView = new ImageView(this);
        iconImageView.setImageResource(R.drawable.baseline_location_on_24); // Set your icon here
        int size = getResources().getDimensionPixelSize(R.dimen.icon_size); // Set icon size
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(size, size);
        params.leftMargin = (int) x - size / 2;
        params.topMargin = (int) y - size / 2;
        rootView.addView(iconImageView, params); // Add icon to root view
    }
}
