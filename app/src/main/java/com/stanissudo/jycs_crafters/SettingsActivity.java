package com.stanissudo.jycs_crafters;

import android.content.Context;
import android.content.Intent;
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

import com.stanissudo.jycs_crafters.database.FuelTrackAppRepository;
import com.stanissudo.jycs_crafters.database.entities.User;

public class SettingsActivity extends AppCompatActivity {

    private FuelTrackAppRepository repository;
    private SharedPreferences sp;

    private TextView headerName;          // @+id/settings_header_name
    private TextView headerUsername;      // @+id/settings_header_username
    private TextView headerAvatar;        // @+id/settings_avatar
    private TextView usernameText;        // @+id/settings_username
    private EditText displayNameEdit;     // @+id/settings_display_name
    private Button saveNameButton;        // @+id/saveDisplayNameButton
    private Button changePasswordButton;  // @+id/changePasswordButton
    private Button deleteAccountButton;   // @+id/deleteAccountButton

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        repository = FuelTrackAppRepository.getRepository(getApplication());
        sp = getSharedPreferences("login_prefs", Context.MODE_PRIVATE);

        // Bind views to IDs from your XML
        headerName = findViewById(R.id.settings_header_name);
        headerUsername = findViewById(R.id.settings_header_username);
        headerAvatar = findViewById(R.id.settings_avatar);
        usernameText = findViewById(R.id.settings_username);
        displayNameEdit = findViewById(R.id.settings_display_name);
        saveNameButton = findViewById(R.id.saveDisplayNameButton);
        changePasswordButton = findViewById(R.id.changePasswordButton);
        deleteAccountButton = findViewById(R.id.deleteAccountButton);

        // Load current session values
        int userId = sp.getInt("userId", -1);
        String currentUsername = sp.getString("username", "");

        // Populate static fields
        usernameText.setText(currentUsername);
        headerUsername.setText("@" + currentUsername);

        // Observe the current user so header + edit field stay in sync
        if (userId > 0) {
            repository.getUserById(userId).observe(this, user -> {
                if (user != null) {
                    String dn = (user.getDisplayName() == null || user.getDisplayName().trim().isEmpty())
                            ? "Display Name" : user.getDisplayName();
                    headerName.setText(dn);
                    if (displayNameEdit.getText().toString().trim().isEmpty()) {
                        displayNameEdit.setText(user.getDisplayName());
                    }
                    // Set simple avatar initial
                    String initial = (dn.trim().isEmpty() ? currentUsername : dn).substring(0, 1).toUpperCase();
                    headerAvatar.setText(initial);
                }
            });
        }

        // Save display name
        saveNameButton.setOnClickListener(v -> {
            String newName = displayNameEdit.getText().toString().trim();
            if (userId <= 0) {
                Toast.makeText(this, "Session error: user not found.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newName.isEmpty()) {
                Toast.makeText(this, "Display name cannot be empty.", Toast.LENGTH_SHORT).show();
                return;
            }
            repository.updateDisplayName(userId, newName);
            headerName.setText(newName);
            headerAvatar.setText(newName.substring(0, 1).toUpperCase());
            Toast.makeText(this, "Name updated.", Toast.LENGTH_SHORT).show();
        });

        // Change password flow
        changePasswordButton.setOnClickListener(v -> showChangePasswordDialog());

        // Soft-delete (deactivate) account
        deleteAccountButton.setOnClickListener(v -> {
            if (userId <= 0) {
                Toast.makeText(this, "Session error: user not found.", Toast.LENGTH_SHORT).show();
                return;
            }
            new AlertDialog.Builder(this)
                    .setTitle("Deactivate Account")
                    .setMessage("This will deactivate your account. You can restore it later. Continue?")
                    .setPositiveButton("Deactivate", (d, w) -> {
                        repository.softDeleteUserById(userId);
                        // Clear session and go back to Login
                        SharedPreferences.Editor ed = sp.edit();
                        ed.putBoolean("isLoggedIn", false);
                        ed.remove("username");
                        ed.remove("isAdmin");
                        ed.apply();
                        startActivity(LoginActivity.intentFactory(this));
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void showChangePasswordDialog() {
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
            String np = newPass.getText().toString().trim();
            String cp = confirmPass.getText().toString().trim();

            if (cur.isEmpty() || np.isEmpty() || cp.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!np.equals(cp)) {
                Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // CAMILA: password complexity (>=8 chars and contains letter + digit)
            if (!isStrongPassword(np)) {
                Toast.makeText(this,
                        "Password must be at least 8 characters and include a letter and a number.",
                        Toast.LENGTH_LONG).show();
                return;
            }

            String currentUsername = sp.getString("username", "");
            repository.changePasswordWithCurrentCheck(currentUsername, cur, np, (ok, msg) -> {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            });
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    // CAMILA: simple strength rule
    private boolean isStrongPassword(String pw) {
        if (pw == null) return false;
        if (pw.length() < 8) return false;
        boolean hasLetter = false, hasDigit = false;
        for (int i = 0; i < pw.length(); i++) {
            char c = pw.charAt(i);
            if (Character.isLetter(c)) hasLetter = true;
            if (Character.isDigit(c)) hasDigit = true;
            if (hasLetter && hasDigit) break;
        }
        return hasLetter && hasDigit;
    }

    public static Intent intentFactory(Context context) {
        return new Intent(context, SettingsActivity.class);
    }
}
