package com.stanissudo.jycs_crafters;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class AdminLandingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_landing);

        Button manageUsersButton = findViewById(R.id.manageUsersButton);
        Button reviewVehiclesButton = findViewById(R.id.reviewVehiclesButton);

        manageUsersButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, UserManagementActivity.class);
            startActivity(intent);
        });

        reviewVehiclesButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, VehicleReviewActivity.class);
            startActivity(intent);
        });
    }
}
