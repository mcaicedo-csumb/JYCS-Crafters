//author: jose

package com.stanissudo.jycs_crafters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;

import com.stanissudo.jycs_crafters.database.FuelTrackAppRepository;
import com.stanissudo.jycs_crafters.database.entities.User;
import com.stanissudo.jycs_crafters.databinding.ActivitySignupBinding;

public class SignupActivity extends AppCompatActivity {

    private ActivitySignupBinding binding;
    private FuelTrackAppRepository repository;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = FuelTrackAppRepository.getRepository(getApplication());
        prefs = getSharedPreferences("login_prefs", MODE_PRIVATE);

        binding.createAccountButton.setOnClickListener(v -> attemptSignup());
        binding.loginLink.setOnClickListener(v ->
                startActivity(LoginActivity.intentFactory(this)));
    }

    private void attemptSignup() {
        String username = binding.usernameInput.getText().toString().trim();
        String password = binding.passwordInput.getText().toString();
        String confirm  = binding.confirmPasswordInput.getText().toString();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirm)) {
            toast("Please fill in all fields");
            return;
        }
        if (password.length() < 6) {
            toast("Password must be at least 6 characters");
            return;
        }
        if (!password.equals(confirm)) {
            toast("Passwords do not match");
            return;
        }

        // Check if user exists
        LiveData<User> userLive = repository.getUserByUsername(username);
        userLive.observe(this, existing -> {
            if (existing != null) {
                toast("That username is already taken");
                userLive.removeObservers(this);
                return;
            }

            // Create user
            User newUser = new User(username, password);
            repository.insertUser(newUser);

            // After insert, observe again to get the row with generated ID
            LiveData<User> createdLive = repository.getUserByUsername(username);
            createdLive.observe(this, created -> {
                if (created != null) {
                    saveUserSession(created.getId(), created.getUsername(), false);
                    toast("Account created — welcome!");
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                    createdLive.removeObservers(this);
                }
            });

            userLive.removeObservers(this);
        });
    }

    private void saveUserSession(int userId, String username, boolean isAdmin) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("userId", userId);
        editor.putString("username", username);
        editor.putBoolean("isAdmin", isAdmin);
        editor.putBoolean("isLoggedIn", true);
        editor.apply();
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public static Intent intentFactory(Context context) {
        return new Intent(context, SignupActivity.class);
    }
}
