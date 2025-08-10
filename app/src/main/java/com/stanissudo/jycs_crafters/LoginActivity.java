//Author: Jose Caicedo
// Date: 08/05/2025

package com.stanissudo.jycs_crafters;

import com.stanissudo.jycs_crafters.network.AdviceResponse;
import com.stanissudo.jycs_crafters.network.AdviceService;
import com.stanissudo.jycs_crafters.network.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;



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

        sharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE);

        // ✅ Auto-login check
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        if (isLoggedIn) {
            String username = sharedPreferences.getString("username", "");
            boolean isAdmin = sharedPreferences.getBoolean("isAdmin", false);

            if (isAdmin) {
                startActivity(LandingPageActivity.intentFactory(this, username, true));
            } else {
                startActivity(new Intent(this, MainActivity.class));
            }
            finish();
            return; // Stop loading login UI
        }

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        loadAdviceInto(binding.quoteText);


        repository = FuelTrackAppRepository.getRepository(getApplication());
        firebaseAuth = FirebaseAuth.getInstance();

        // Google Sign-In config
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        binding.loginButton.setOnClickListener(v -> verifyUser());

        binding.googleSignInButton.setOnClickListener(v -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    private void loadAdviceInto(android.widget.TextView target) {
        AdviceService service = RetrofitClient.getInstance().create(AdviceService.class);
        service.getAdvice().enqueue(new Callback<AdviceResponse>() {
            @Override
            public void onResponse(Call<AdviceResponse> call, Response<AdviceResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getSlip() != null) {
                    String text = "“" + response.body().getSlip().getAdvice() + "”";
                    target.setText(text);
                } else {
                    target.setText("Could not load advice.");
                }
            }
            @Override
            public void onFailure(Call<AdviceResponse> call, Throwable t) {
                target.setText("Error: " + (t.getMessage() != null ? t.getMessage() : "Unknown"));
            }
        });
    }


    private void verifyUser() {
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
                    saveUserSession(user.getId(), user.getUsername(), user.isAdmin());

                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();

                    if (user.isAdmin()) {
                        startActivity(LandingPageActivity.intentFactory(this, user.getUsername(), true));
                    } else {
                        startActivity(new Intent(this, MainActivity.class));
                    }
                    finish();
                } else {
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
                                    saveUserSession(newUser.getId(), email, false);
                                    startActivity(new Intent(this, MainActivity.class));
                                } else {
                                    saveUserSession(user.getId(), user.getUsername(), user.isAdmin());
                                    if (user.isAdmin()) {
                                        startActivity(LandingPageActivity.intentFactory(this, user.getUsername(), true));
                                    } else {
                                        startActivity(new Intent(this, MainActivity.class));
                                    }
                                }
                                finish();
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

    // ✅ Save login state for persistence
    private void saveUserSession(int userId, String username, boolean isAdmin) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("userId", userId);
        editor.putString("username", username);
        editor.putBoolean("isAdmin", isAdmin);
        editor.putBoolean("isLoggedIn", true);
        editor.apply();
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public static Intent intentFactory(Context context) {
        return new Intent(context, LoginActivity.class);
    }
}
