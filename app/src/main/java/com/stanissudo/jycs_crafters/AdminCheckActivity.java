/**
 * Author: Jose
 * Date: 2025-08-13
 *
 * Activity for managing administrative actions within the FuelTrack application.
 * This screen is only accessible to admin users and provides:
 * - Viewing all registered users in a RecyclerView
 * - Adding new users
 * - Removing users
 * - Changing user passwords
 * - Maria: Deactivating, reactivating, and permanently deleting users
 * - Logging out of the application
 *
 * Notes:
 * - Uses LiveData to observe user list changes in real-time.
 * - Relies on repository-level safety checks for operations such as avoiding self-deletion.
 * - Includes both Firebase and Google Sign-In logout support.
 */

package com.stanissudo.jycs_crafters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.stanissudo.jycs_crafters.database.FuelTrackAppRepository;
import com.stanissudo.jycs_crafters.databinding.ActivityAdminCheckBinding;

public class AdminCheckActivity extends AppCompatActivity {

    private ActivityAdminCheckBinding binding;
    private FuelTrackAppRepository repository;
    private SharedPreferences sharedPreferences;
    private UserListAdapter adapter;

    /**
     * Initializes the admin view, sets up UI components, binds click listeners,
     * and loads the user list for management.
     *
     * @param savedInstanceState The saved instance state bundle.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminCheckBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sharedPreferences = getSharedPreferences("login_prefs", MODE_PRIVATE);
        repository = FuelTrackAppRepository.getRepository(getApplication());

        binding.adminMessage.setText("You have admin privileges!");

        // RecyclerView setup
        adapter = new UserListAdapter();
        binding.userListRecycler.setLayoutManager(new LinearLayoutManager(this));
        binding.userListRecycler.setAdapter(adapter);

        // Observe and update user list dynamically
        repository.getAllUsers().observe(this, users -> adapter.setUsers(users));

        // Button actions
        binding.addUserButton.setOnClickListener(v -> showAddUserDialog());
        binding.removeUserButton.setOnClickListener(v -> showRemoveUserDialog());
        binding.changePasswordButton.setOnClickListener(v -> showChangePasswordDialog());
        binding.logoutButton.setOnClickListener(v -> logout());

        // Maria: Extra admin actions (Deactivate / Reactivate / Delete permanently)
        if (binding.deactivateUserButton != null) {
            binding.deactivateUserButton.setOnClickListener(v -> showDeactivateUserDialog());
        }
        if (binding.reactivateUserButton != null) {
            binding.reactivateUserButton.setOnClickListener(v -> showReactivateUserDialog());
        }
        if (binding.deleteUserPermButton != null) {
            binding.deleteUserPermButton.setOnClickListener(v -> showDeleteUserDialog());
        }
    }

    /**
     * Displays a dialog for adding a new user.
     * Validates inputs and delegates user creation to the repository.
     */
    private void showAddUserDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add User");

        EditText usernameInput = new EditText(this);
        usernameInput.setHint("Username");

        EditText passwordInput = new EditText(this);
        passwordInput.setHint("Password");
        passwordInput.setInputType(
                android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        );

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad, pad, 0);
        layout.addView(usernameInput);
        layout.addView(passwordInput);
        builder.setView(layout);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            repository.addUserSafely(username, password, false, (ok, msg) -> {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            });
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    /**
     * Displays a dialog for removing a user by username.
     * Prevents deletion of the currently logged-in user unless allowed by repository rules.
     */
    private void showRemoveUserDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Remove User");

        EditText usernameInput = new EditText(this);
        usernameInput.setHint("Username");
        builder.setView(usernameInput);

        builder.setPositiveButton("Remove", (dialog, which) -> {
            String username = usernameInput.getText().toString().trim();
            if (username.isEmpty()) {
                Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show();
                return;
            }

            String currentUsername = sharedPreferences.getString("username", "");
            boolean isAdmin = sharedPreferences.getBoolean("isAdmin", false);

            repository.deleteUserSafely(username, currentUsername, isAdmin, (ok, msg) -> {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            });
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    /**
     * Maria: Shows a dialog to deactivate a user (sets isActive = 0).
     */
    private void showDeactivateUserDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Deactivate User");

        EditText usernameInput = new EditText(this);
        usernameInput.setHint("Username");
        builder.setView(usernameInput);

        builder.setPositiveButton("Deactivate", (dialog, which) -> {
            String username = usernameInput.getText().toString().trim();
            if (username.isEmpty()) {
                Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show();
                return;
            }

            String currentUsername = sharedPreferences.getString("username", "");
            boolean isAdmin = sharedPreferences.getBoolean("isAdmin", false);

            repository.deactivateUserSafely(username, currentUsername, isAdmin,
                    (ok, msg) -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    /**
     * Maria: Shows a dialog to reactivate a user (sets isActive = 1).
     */
    private void showReactivateUserDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reactivate User");

        EditText usernameInput = new EditText(this);
        usernameInput.setHint("Username");
        builder.setView(usernameInput);

        builder.setPositiveButton("Reactivate", (dialog, which) -> {
            String username = usernameInput.getText().toString().trim();
            if (username.isEmpty()) {
                Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show();
                return;
            }

            String currentUsername = sharedPreferences.getString("username", "");
            boolean isAdmin = sharedPreferences.getBoolean("isAdmin", false);

            repository.reactivateUserSafely(username, currentUsername, isAdmin,
                    (ok, msg) -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    /**
     * Maria: Shows a dialog to permanently delete a user, with confirmation prompt.
     */
    private void showDeleteUserDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete User Permanently");

        EditText usernameInput = new EditText(this);
        usernameInput.setHint("Username");
        builder.setView(usernameInput);

        builder.setPositiveButton("Delete", (dialog, which) -> {
            String username = usernameInput.getText().toString().trim();
            if (username.isEmpty()) {
                Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show();
                return;
            }

            String currentUsername = sharedPreferences.getString("username", "");
            boolean isAdmin = sharedPreferences.getBoolean("isAdmin", false);

            new AlertDialog.Builder(this)
                    .setTitle("Confirm Permanent Delete")
                    .setMessage("This will permanently delete the user '" + username + "'. Continue?")
                    .setPositiveButton("Yes, delete", (d, w) ->
                            repository.deleteUserSafely(username, currentUsername, isAdmin,
                                    (ok, msg) -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()))
                    .setNegativeButton("Cancel", null)
                    .show();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    /**
     * Displays a dialog for changing the current user's password.
     * Validates new password and confirms match before updating.
     */
    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Password");

        EditText newPassInput = new EditText(this);
        newPassInput.setHint("New Password");
        newPassInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

        EditText confirmPassInput = new EditText(this);
        confirmPassInput.setHint("Confirm New Password");
        confirmPassInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad, pad, 0);
        layout.addView(newPassInput);
        layout.addView(confirmPassInput);
        builder.setView(layout);

        builder.setPositiveButton("Change", (dialog, which) -> {
            String newPass = newPassInput.getText().toString().trim();
            String confirmPass = confirmPassInput.getText().toString().trim();

            if (newPass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newPass.equals(confirmPass)) {
                Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences sp = getSharedPreferences("login_prefs", Context.MODE_PRIVATE);
            String currentUsername = sp.getString("username", "");

            repository.changePasswordIfUserExists(currentUsername, newPass, (ok, msg) -> {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            });
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    /**
     * Logs out the current user from Firebase, Google Sign-In, and clears stored preferences.
     */
    private void logout() {
        FirebaseAuth.getInstance().signOut();

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(
                this,
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        );

        googleSignInClient.signOut().addOnCompleteListener(task -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isLoggedIn", false);
            editor.remove("username");
            editor.remove("isAdmin");
            editor.apply();

            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    /**
     * Factory method for creating an intent to launch this activity.
     *
     * @param context The calling context.
     * @return An Intent configured for AdminCheckActivity.
     */
    public static Intent intentFactory(Context context) {
        return new Intent(context, AdminCheckActivity.class);
    }
}
