package com.stanissudo.jycs_crafters;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;

import com.stanissudo.jycs_crafters.database.FuelTrackAppRepository;
import com.stanissudo.jycs_crafters.database.entities.User;

public class LoginActivity extends AppCompatActivity {

    private EditText inputUsername, inputPassword;
    private TextView statusText;
    private FuelTrackAppRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Init repository
        repository = FuelTrackAppRepository.getRepository(getApplication());

        // Bind views
        inputUsername = findViewById(R.id.inputUsername);
        inputPassword = findViewById(R.id.inputPassword);
        statusText = findViewById(R.id.statusText);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnCreateTestUser = findViewById(R.id.btnCreateTestUser);

        // Login button logic
        btnLogin.setOnClickListener(v -> {
            String username = inputUsername.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                statusText.setText("Please enter username and password.");
                return;
            }

            // Observe once (avoid piling observers on multiple clicks)
            LiveData<User> live = repository.getUserByUsername(username);
            live.observe(this, user -> {
                // remove this observer right away
                live.removeObservers(this);

                if (user != null && password.equals(user.getPassword())) {
                    statusText.setText("Login successful!");

                    if (user.isAdmin()) {
                        // -> Camila’s AdminLandingActivity (pass extras)
                        Intent intent = new Intent(LoginActivity.this, AdminLandingActivity.class);
                        intent.putExtra(AdminLandingActivity.EXTRA_USERNAME, user.getUsername());
                        intent.putExtra(AdminLandingActivity.EXTRA_IS_ADMIN, user.isAdmin());
                        intent.putExtra(AdminLandingActivity.EXTRA_USER_ID, user.getId());
                        startActivity(intent);
                    } else {
                        // -> regular user landing page (pass what you need)
                        Intent intent = new Intent(LoginActivity.this, LandingPageActivity.class);
                        // If LandingPageActivity expects extras, add them here:
                        // intent.putExtra("username", user.getUsername());
                        startActivity(intent);
                    }
                } else {
                    statusText.setText("Invalid username or password.");
                }
            });
        });

        // Create Test User button (for quick testing)
        // TODO: REMOVE BEFORE FINAL SUBMISSION (temporary seeding for demo)
        btnCreateTestUser.setOnClickListener(v -> {
            User adminUser = new User("admin", "1234", true);
            User regularUser = new User("user", "pass", false);
            repository.insertUser(adminUser);
            repository.insertUser(regularUser);
            statusText.setText("Test users created: admin/1234 (admin), user/pass (regular)");
            Log.d("LoginActivity", "Inserted test users into DB.");
        });
    }
}
