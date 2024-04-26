package com.example.letsgo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.letsgo.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding ;

    @Override
    protected void onCreate(Bundle savedInstance ) {
        super.onCreate(savedInstance);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.adminLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adminLogin();
            }
        });

        binding.user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userAccess();
            }
        });
    }

    private void adminLogin(){
        Intent intent = new Intent(MainActivity.this, AdminAuthenticationActivity.class);
        startActivity(intent);
    }

    private void userAccess(){
        Intent intent = new Intent(MainActivity.this, UserShowMapsActivity.class);
        startActivity(intent);
    }


}
