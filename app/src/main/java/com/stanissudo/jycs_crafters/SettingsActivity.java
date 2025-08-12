package com.stanissudo.jycs_crafters;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;

import com.stanissudo.jycs_crafters.database.FuelTrackAppRepository;
import com.stanissudo.jycs_crafters.database.entities.User;

public class SettingsActivity extends AppCompatActivity {

    private FuelTrackAppRepository repository;

    private TextView usernameView;
    private EditText displayNameInput;
    private Button saveNameBtn;
    private Button changePassBtn;
    private Button deleteAccountBtn;

    private SharedPreferences sp;

    // CAMILA: pretty header (optional views; safe if missing in XML)
    private TextView headerName;      // CAMILA
    private TextView headerUsername;  // CAMILA
    private TextView headerAvatar;    // CAMILA

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        repository = FuelTrackAppRepository.getRepository(getApplication());
        sp = getSharedPreferences("login_prefs", Context.MODE_PRIVATE);

        // CAMILA: show @username in the title bar
        setTitle("Settings — @" + sp.getString("username", "")); // CAMILA

        usernameView     = findViewById(R.id.settings_username);
        displayNameInput = findViewById(R.id.settings_display_name);
        saveNameBtn      = findViewById(R.id.saveDisplayNameButton);
        changePassBtn    = findViewById(R.id.changePasswordButton);
        deleteAccountBtn = findViewById(R.id.deleteAccountButton);

        // CAMILA: bind pretty header (if you added these IDs in XML)
        headerName     = findViewById(R.id.settings_header_name);     // CAMILA
        headerUsername = findViewById(R.id.settings_header_username); // CAMILA
        headerAvatar   = findViewById(R.id.settings_avatar);          // CAMILA

        int userId = sp.getInt("userId", -1);
        if (userId <= 0) {
            Toast.makeText(this, "No user session.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        LiveData<User> userLive = repository.getUserById(userId);
        userLive.observe(this, u -> {
            if (u == null) return;

            usernameView.setText(u.getUsername());
            String dn = u.getDisplayName();
            displayNameInput.setText(dn == null || dn.isEmpty() ? u.getUsername() : dn);

            // CAMILA: fill the header nicely if present
            if (headerName != null) headerName.setText(displayNameInput.getText().toString());         // CAMILA
            if (headerUsername != null) headerUsername.setText("@" + u.getUsername());                 // CAMILA
            if (headerAvatar != null && u.getUsername().length() > 0)                                  // CAMILA
                headerAvatar.setText(String.valueOf(u.getUsername().charAt(0)).toUpperCase());         // CAMILA
        });

        if (saveNameBtn != null) {
            saveNameBtn.setOnClickListener(v -> {
                String newName = displayNameInput.getText().toString().trim();
                if (newName.isEmpty()) {
                    Toast.makeText(this, "Display name cannot be empty.", Toast.LENGTH_SHORT).show();
                    return;
                }
                repository.updateDisplayName(userId, newName);
                Toast.makeText(this, "Name updated.", Toast.LENGTH_SHORT).show();

                // reflect change in header immediately if present
                if (headerName != null) headerName.setText(newName);
            });
        }

        if (changePassBtn != null) {
            changePassBtn.setOnClickListener(v -> showChangePasswordDialog(userId));
        }

        if (deleteAccountBtn != null) {
            deleteAccountBtn.setOnClickListener(v ->
                    new AlertDialog.Builder(this)
                            .setTitle("Deactivate Account")
                            .setMessage("This will deactivate your account. You can ask an admin to reactivate it later.")
                            .setPositiveButton("Deactivate", (d, w) -> {
                                repository.softDeleteUserById(userId);
                                SharedPreferences.Editor ed = sp.edit();
                                ed.putBoolean("isLoggedIn", false);
                                ed.remove("username");
                                ed.remove("userId");
                                ed.remove("isAdmin");
                                ed.apply();
                                startActivity(LoginActivity.intentFactory(this));
                                finish();
                            })
                            .setNegativeButton("Cancel", null)
                            .show()
            );
        }
    }

    private void showChangePasswordDialog(int userId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Password");

        EditText currentPass = new EditText(this);
        currentPass.setHint("Current Password");
        currentPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        EditText newPass = new EditText(this);
        newPass.setHint("New Password");
        newPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        EditText confirmPass = new EditText(this);
        confirmPass.setHint("Confirm New Password");
        confirmPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad, pad, 0);
        layout.addView(currentPass);
        layout.addView(newPass);
        layout.addView(confirmPass);
        builder.setView(layout);

        builder.setPositiveButton("Change", (dialog, which) -> {
            String cur = currentPass.getText().toString().trim();
            String np  = newPass.getText().toString().trim();
            String cp  = confirmPass.getText().toString().trim();

            if (cur.isEmpty() || np.isEmpty() || cp.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!np.equals(cp)) {
                Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }
            if (np.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters.", Toast.LENGTH_SHORT).show();
                return;
            }

            LiveData<User> live = repository.getUserById(userId);
            live.observe(this, u -> {
                if (u == null) return;

                String stored = u.getPassword();
                boolean storedIsHash = stored != null && stored.matches("(?i)^[0-9a-f]{64}$");
                String curHash = sha256(cur);

                boolean ok = storedIsHash ? stored.equalsIgnoreCase(curHash) : stored.equals(cur);
                if (!ok) {
                    Toast.makeText(this, "Current password is incorrect.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String newHash = sha256(np);
                repository.updatePasswordById(userId, newHash);
                Toast.makeText(this, "Password updated.", Toast.LENGTH_SHORT).show();
            });
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void confirmSoftDelete(int userId) {
        // kept inside onClick above; here only if you want to call directly
    }

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

    public static android.content.Intent intentFactory(Context context) {
        return new android.content.Intent(context, SettingsActivity.class);
    }
}
