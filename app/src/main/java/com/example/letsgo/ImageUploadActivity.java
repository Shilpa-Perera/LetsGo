package com.example.letsgo;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.letsgo.databinding.ActivityImageUploadBinding;
import com.example.letsgo.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageUploadActivity extends AppCompatActivity {
    ActivityImageUploadBinding binding ;
    Uri imageUri;
    StorageReference storageReference;

    FirebaseFirestore firestore;
    String mapDocId ;

    @Override
    protected void onCreate(Bundle savedInstance ) {
        super.onCreate(savedInstance);
        binding = ActivityImageUploadBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });

        binding.uploadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
            }
        });
    }

    private void uploadImage() {

        AlertDialog.Builder builder = new AlertDialog.Builder(ImageUploadActivity.this);
        builder.setTitle("Please Enter Map Name");

        final EditText input = new EditText(ImageUploadActivity.this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Upload", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String mapName = input.getText().toString().trim(); // Get the entered map name

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy_dd_HH_mm_ss", Locale.US);
                Date now = new Date();
                String fileName = formatter.format(now);
                storageReference = FirebaseStorage.getInstance().getReference("images/"+fileName);
                storageReference.putFile(imageUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Toast.makeText(ImageUploadActivity.this, "Successfully uploaded", Toast.LENGTH_SHORT).show();
                                showImage(imageUri);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ImageUploadActivity.this, "Failed uploaded",
                                        Toast.LENGTH_SHORT).show();

                            }
                        });
                firestore = FirebaseFirestore.getInstance();
                MapInfo mapInfo = new MapInfo(storageReference.getPath(),-1,
                        -1, mapName);
                firestore.collection("map")
                        .add(mapInfo)
                        .addOnSuccessListener(documentReference -> {
                            // Document has been added successfully
                            mapDocId = documentReference.getId(); // Get the generated document ID
                            // You can now use the document ID as needed
                            Log.d("Firebase", "DocumentSnapshot written with ID: " + mapDocId);
                        })
                        .addOnFailureListener(e -> {
                            // Handle errors
                            Log.e("Firebase", "Error adding document", e);
                        });;
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();

    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        activityResultLauncher.launch(intent);

    }

    private void showImage(Uri imageUri){
        Intent intent = new Intent(ImageUploadActivity.this, ReferencePointActivity.class);
        intent.putExtra("imageUri",imageUri.toString());
        intent.putExtra("mapDocId" , mapDocId);
        startActivity(intent);
    }

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode() == Activity.RESULT_OK){
                        Intent data = result.getData();
                        imageUri = data.getData();
                    }
                }
            }
    );
}
