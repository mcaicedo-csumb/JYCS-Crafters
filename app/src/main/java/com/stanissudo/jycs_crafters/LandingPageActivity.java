package com.stanissudo.jycs_crafters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.stanissudo.jycs_crafters.databinding.ActivityLandingPageBinding;

public class LandingPageActivity extends AppCompatActivity {

    private static final String EXTRA_USERNAME = "com.stanissudo.USERNAME";
    private static final String EXTRA_IS_ADMIN = "com.stanissudo.IS_ADMIN";

    private ActivityLandingPageBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLandingPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String username = getIntent().getStringExtra(EXTRA_USERNAME);
        boolean isAdmin = getIntent().getBooleanExtra(EXTRA_IS_ADMIN, false);

        binding.greetingText.setText("Welcome, " + username);

        // ✅ Add this toast to confirm the activity opened
        Toast.makeText(this, "Welcome to the Landing Page!", Toast.LENGTH_SHORT).show();

        if (isAdmin) {
            binding.adminCheckButton.setOnClickListener(v -> {
                startActivity(AdminCheckActivity.intentFactory(this));
            });
            binding.adminCheckButton.setEnabled(true);
        } else {
            binding.adminCheckButton.setEnabled(false);
        }
    }


    public static Intent intentFactory(Context context, String username, boolean isAdmin) {
        Intent intent = new Intent(context, LandingPageActivity.class);
        intent.putExtra(EXTRA_USERNAME, username);
        intent.putExtra(EXTRA_IS_ADMIN, isAdmin);
        return intent;
    }
}
