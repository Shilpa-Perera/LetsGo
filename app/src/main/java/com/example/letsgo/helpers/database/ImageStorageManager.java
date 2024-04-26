package com.example.letsgo.helpers.database;

import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.letsgo.activities.ImageUploadActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class ImageStorageManager {
    private StorageReference storageReference;

    public ImageStorageManager(String fileName) {
        this.storageReference = FirebaseStorage.getInstance().getReference("images/"+fileName);
    }

}
