package com.stanissudo.jycs_crafters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import com.stanissudo.jycs_crafters.database.FuelTrackAppRepository;
import com.stanissudo.jycs_crafters.database.entities.User;
import com.stanissudo.jycs_crafters.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1001;

    private ActivityLoginBinding binding;
    private FuelTrackAppRepository repository;
    private SharedPreferences sharedPreferences;

    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = FuelTrackAppRepository.getRepository(getApplication());
        sharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE);
        firebaseAuth = FirebaseAuth.getInstance();

        // Google Sign-In config
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))  // From google-services.json
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        binding.loginButton.setOnClickListener(v -> verifyUser());

        // FIXED ID: googleSignInButton
        binding.googleSignInButton.setOnClickListener(v -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    private void verifyUser() {
        // FIXED ID: usernameInput and passwordInput
        String username = binding.usernameInput.getText().toString().trim();
        String password = binding.passwordInput.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showToast("Please enter username and password");
            return;
        }

        LiveData<User> userLiveData = repository.getUserByUsername(username);
        userLiveData.observe(this, user -> {
            if (user != null) {
                if (user.getPassword().equals(password)) {
                    saveUserSession(user.getId());
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show(); // ✅ Add this line
                    startActivity(LandingPageActivity.intentFactory(this, user.getUsername(), user.isAdmin()));
                }
                else {
                    showToast("Incorrect password");
                }
            } else {
                showToast("User not found");
            }
        });
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String email = firebaseUser.getEmail();
                            if (email == null) {
                                showToast("Google account has no email associated.");
                                return;
                            }
                            LiveData<User> userLiveData = repository.getUserByUsername(email);

                            userLiveData.observe(this, user -> {
                                if (user == null) {
                                    User newUser = new User(email, "oauth_dummy");
                                    repository.insertUser(newUser);
                                    showToast("New Google user added.");
                                }
                                startActivity(LandingPageActivity.intentFactory(this, email, false));
                            });
                        }
                    } else {
                        showToast("Google login failed.");
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                showToast("Google sign-in error");
            }
        }
    }

    private void saveUserSession(int userId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("userId", userId);
        editor.apply();
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public static Intent intentFactory(Context context) {
        return new Intent(context, LoginActivity.class);
    }
}
