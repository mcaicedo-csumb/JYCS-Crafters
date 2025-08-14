/**
 * Author: Jose
 * Date: 2025-08-13
 *
 * This Activity handles the user sign-up process for the FuelTrack application.
 * It provides:
 * - Input validation for username and password
 * - Checking if the username already exists
 * - Creating a new user in the Room database
 * - Automatically logging in the new user after successful registration
 *
 * Notes:
 * - Uses LiveData observers to check for existing usernames and retrieve the newly created user with its generated ID.
 * - Stores login session in SharedPreferences for persistent login across app restarts.
 */

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

    /**
     * Called when the activity is first created.
     * Initializes the UI, binds event listeners, and sets up the repository and preferences.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the data it most recently supplied.
     */
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

    /**
     * Attempts to sign up the user.
     * - Validates the username and password fields.
     * - Ensures password meets minimum length and matches confirmation.
     * - Checks if the username already exists in the database.
     * - If valid, inserts the new user and logs them in immediately.
     */
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

    /**
     * Saves the user session to SharedPreferences.
     *
     * @param userId   The ID of the user.
     * @param username The username of the user.
     * @param isAdmin  Whether the user has admin privileges.
     */
    private void saveUserSession(int userId, String username, boolean isAdmin) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("userId", userId);
        editor.putString("username", username);
        editor.putBoolean("isAdmin", isAdmin);
        editor.putBoolean("isLoggedIn", true);
        editor.apply();
    }

    /**
     * Displays a Toast message on the screen.
     *
     * @param msg The message to display.
     */
    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * Factory method for creating an intent to start this activity.
     *
     * @param context The context from which the activity will be started.
     * @return The intent to launch SignupActivity.
     */
    public static Intent intentFactory(Context context) {
        return new Intent(context, SignupActivity.class);
    }
}
