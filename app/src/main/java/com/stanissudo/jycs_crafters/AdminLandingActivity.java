package com.stanissudo.jycs_crafters;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/** Camila: AdminLandingActivity – entry point for my admin-only tools */
public class AdminLandingActivity extends AppCompatActivity {

    // Camila: extras used by LoginActivity when routing admins here
    public static final String EXTRA_USERNAME = "extra_username";
    public static final String EXTRA_IS_ADMIN = "extra_is_admin";
    public static final String EXTRA_USER_ID  = "extra_user_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_landing);

        TextView title = findViewById(R.id.txtTitle); // Camila: matches layout id
        Button btnUserMgmt = findViewById(R.id.btnUserMgmt);
        Button btnVehicleReview = findViewById(R.id.btnVehicleReview);

        String username = getIntent().getStringExtra(EXTRA_USERNAME);
        if (username != null && !username.isEmpty()) {
            title.setText("Admin: " + username);
        }

        btnUserMgmt.setOnClickListener(v ->
                startActivity(new Intent(this, UserManagementActivity.class)));

        btnVehicleReview.setOnClickListener(v ->
                startActivity(new Intent(this, VehicleReviewActivity.class)));
    }
}
