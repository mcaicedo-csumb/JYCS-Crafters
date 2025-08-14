/**
 * Author: Jose Caicedo
 * Date: 08/05/2025
 *
 * This Activity handles the login process for the FuelTrack application.
 * It supports:
 * - Manual username/password login
 * - Google Sign-In with Firebase Authentication
 * - Session persistence using SharedPreferences
 * - Loading motivational advice from a remote API via Retrofit
 *
 * Maria's contributions:
 * - Blocking login for inactive users
 * - Supporting hashed and legacy plaintext passwords
 * - Automatically upgrading legacy plaintext passwords to hashed values
 * - Blocking Google Sign-In for inactive users
 * - SHA-256 helper for password hashing
 *
 * The class ensures secure authentication, prevents access to deactivated accounts,
 * and routes users to appropriate pages (admin landing or main screen) after login.
 */

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

import com.stanissudo.jycs_crafters.auth.UserSessionManager;

public class LoginActivity extends AppCompatActivity {

    /** Request code for Google Sign-In intent. */
    private static final int RC_SIGN_IN = 1001;

    private ActivityLoginBinding binding;
    private FuelTrackAppRepository repository;
    private SharedPreferences sharedPreferences;
    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth firebaseAuth;

    /**
     * Initializes the login screen, checks for saved sessions, sets up UI bindings,
     * loads a motivational quote, and configures Google Sign-In.
     *
     * @param savedInstanceState Saved state bundle for restoring state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE);

        // Auto-login check
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        if (isLoggedIn) {
            String username = sharedPreferences.getString("username", "");
            boolean isAdmin = sharedPreferences.getBoolean("isAdmin", false);

            int userId = sharedPreferences.getInt("userId", -1);
            UserSessionManager.setSession(this, userId, isAdmin);
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
        binding.signUpButton.setOnClickListener(v ->
                startActivity(SignupActivity.intentFactory(this)));
        binding.googleSignInButton.setOnClickListener(v -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    /**
     * Loads a random advice/quote from an API and displays it in a target TextView.
     * Uses Retrofit for network calls.
     *
     * @param target TextView where the advice will be displayed.
     */
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

    /**
     * Verifies the user’s credentials for manual login.
     * - Maria: blocks inactive users
     * - Supports both hashed and plaintext passwords
     * - Maria: upgrades plaintext to hashed on successful login
     * - Saves session and redirects to appropriate screen.
     */
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
                // Maria: block login for inactive users
                if (!user.isActive()) {
                    showToast("Your account is deactivated. Contact an admin.");
                    return;
                }

                // Maria: support hashed or legacy plaintext passwords
                String stored = user.getPassword();
                boolean storedIsHash = stored != null && stored.matches("(?i)^[0-9a-f]{64}$");
                String attemptHash = sha256(password);

                boolean ok = storedIsHash ? stored.equalsIgnoreCase(attemptHash) : stored.equals(password);
                if (!ok) {
                    showToast("Incorrect password");
                    return;
                }

                // Maria: upgrade legacy plaintext to hash on successful login
                if (!storedIsHash) {
                    repository.updatePasswordById(user.getId(), attemptHash);
                }

                saveUserSession(user.getId(), user.getUsername(), user.isAdmin());
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();

                if (user.isAdmin()) {
                    startActivity(LandingPageActivity.intentFactory(this, user.getUsername(), true));
                } else {
                    startActivity(new Intent(this, MainActivity.class));
                }
                finish();
            } else {
                showToast("User not found");
            }
        });
    }

    /**
     * Signs in the user with Google OAuth credentials through Firebase Authentication.
     * - Creates a new user entry if one doesn’t exist.
     * - Maria: blocks Google login for inactive users.
     *
     * @param idToken Google ID token from Sign-In.
     */
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
                                    // Maria: block Google login for inactive users
                                    if (!user.isActive()) {
                                        showToast("Your account is deactivated. Contact an admin.");
                                        return;
                                    }
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

    /**
     * Handles the result from Google Sign-In intent.
     */
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

    /**
     * Saves the login session data to SharedPreferences for persistent login.
     *
     * @param userId   The ID of the logged-in user.
     * @param username The username of the logged-in user.
     * @param isAdmin  Whether the user has admin privileges.
     */
    private void saveUserSession(int userId, String username, boolean isAdmin) {
        if (sharedPreferences == null) {
            sharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE);
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("userId", userId);
        editor.putString("username", username);
        editor.putBoolean("isAdmin", isAdmin);
        editor.putBoolean("isLoggedIn", true);
        editor.apply();
    }

    /**
     * Displays a Toast message.
     *
     * @param msg The message to display.
     */
    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * Factory method for creating an intent to launch this activity.
     *
     * @param context The context from which the activity will be started.
     * @return The intent to launch LoginActivity.
     */
    public static Intent intentFactory(Context context) {
        return new Intent(context, LoginActivity.class);
    }

    /**
     * Camila: Helper method to compute SHA-256 hash of a string.
     * Used for password storage and verification.
     *
     * @param s The input string.
     * @return The SHA-256 hash in hexadecimal format.
     */
    private static String sha256(String s) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(s.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
