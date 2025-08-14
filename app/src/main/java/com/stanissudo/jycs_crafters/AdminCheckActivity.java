package com.stanissudo.jycs_crafters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

import com.stanissudo.jycs_crafters.auth.UserSessionManager;
import com.stanissudo.jycs_crafters.database.FuelTrackAppRepository;
import com.stanissudo.jycs_crafters.database.entities.Vehicle;
import com.stanissudo.jycs_crafters.databinding.ActivityAdminCheckBinding;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminCheckActivity extends AppCompatActivity {

    private ActivityAdminCheckBinding binding;
    private FuelTrackAppRepository repository;
    private SharedPreferences sharedPreferences;
    private UserListAdapter adapter;

    // For admin bulk transfer dialog
    private java.util.List<Vehicle> vehiclesCache = new java.util.ArrayList<>();

    // Executes admin DB writes off the UI thread
    private final ExecutorService adminExec = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAdminCheckBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sharedPreferences = getSharedPreferences("login_prefs", MODE_PRIVATE);
        repository = FuelTrackAppRepository.getRepository(getApplication());

        binding.adminMessage.setText("You have admin privileges!");

        // Users RecyclerView
        adapter = new UserListAdapter();
        binding.userListRecycler.setLayoutManager(new LinearLayoutManager(this));
        binding.userListRecycler.setAdapter(adapter);

        // Admin-only: list ALL users (gated at repository)
        repository.adminGetAllUsers(this).observe(this, users -> adapter.setUsers(users));

        // Admin-only: load ALL vehicles for transfer dialog (gated at repository)
        repository.adminGetAllVehicles(this).observe(this, vehicles -> {
            vehiclesCache = (vehicles != null) ? vehicles : new java.util.ArrayList<>();
        });

        // Hide admin-only UI if not admin (guard still enforced in repo)
        if (!UserSessionManager.isAdmin(this)) {
            if (binding.btnBulkReassign != null) binding.btnBulkReassign.setVisibility(View.GONE);
        }

        // Wire admin bulk transfer button
        if (binding.btnBulkReassign != null) {
            binding.btnBulkReassign.setOnClickListener(v -> showBulkReassignDialog());
        }

        // Buttons
        binding.addUserButton.setOnClickListener(v -> showAddUserDialog());
        binding.removeUserButton.setOnClickListener(v -> showRemoveUserDialog());
        binding.changePasswordButton.setOnClickListener(v -> showChangePasswordDialog());
        binding.logoutButton.setOnClickListener(v -> logout());

        // Maria: Extra admin actions
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adminExec.shutdown(); // tidy up the background thread
    }

    // ---------- Admin dialogs ----------

    private void showBulkReassignDialog() {
        if (vehiclesCache == null || vehiclesCache.size() < 2) {
            Toast.makeText(this, "Need at least two vehicles.", Toast.LENGTH_SHORT).show();
            return;
        }

        java.util.List<String> names = new java.util.ArrayList<>();
        for (Vehicle v : vehiclesCache) {
            names.add("#" + v.getVehicleID() + " • " + v.getName());
        }

        Spinner fromSpinner = new Spinner(this);
        Spinner toSpinner   = new Spinner(this);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, names);
        fromSpinner.setAdapter(adapter);
        toSpinner.setAdapter(adapter);

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int p = (int) (16 * getResources().getDisplayMetrics().density);
        container.setPadding(p, p, p, p);

        TextView tvFrom = new TextView(this);
        tvFrom.setText("From vehicle");
        TextView tvTo = new TextView(this);
        tvTo.setText("To vehicle");

        container.addView(tvFrom);
        container.addView(fromSpinner);
        container.addView(tvTo);
        container.addView(toSpinner);

        new AlertDialog.Builder(this)
                .setTitle("Transfer Logs (Admin)")
                .setView(container)
                .setPositiveButton("Transfer", (d, w) -> {
                    int fromIdx = fromSpinner.getSelectedItemPosition();
                    int toIdx   = toSpinner.getSelectedItemPosition();
                    if (fromIdx == toIdx) {
                        Toast.makeText(this, "Choose two different vehicles.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    final int oldVehicleId = vehiclesCache.get(fromIdx).getVehicleID();
                    final int newVehicleId = vehiclesCache.get(toIdx).getVehicleID();

                    // Run the bulk UPDATE off the UI thread
                    adminExec.execute(() -> {
                        try {
                            int changed = repository.adminBulkReassignEntries(getApplicationContext(),
                                    oldVehicleId, newVehicleId);

                            runOnUiThread(() ->
                                    Toast.makeText(this, "Moved " + changed + " logs", Toast.LENGTH_LONG).show()
                            );
                        } catch (SecurityException se) {
                            runOnUiThread(() ->
                                    Toast.makeText(this, "Admin only", Toast.LENGTH_SHORT).show()
                            );
                        } catch (Exception e) {
                            runOnUiThread(() ->
                                    Toast.makeText(this, "Transfer failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                            );
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAddUserDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add User");

        EditText usernameInput = new EditText(this);
        usernameInput.setHint("Username");

        EditText passwordInput = new EditText(this);
        passwordInput.setHint("Password");
        passwordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
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

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Password");

        EditText newPassInput = new EditText(this);
        newPassInput.setHint("New Password");
        newPassInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

        EditText confirmPassInput = new EditText(this);
        confirmPassInput.setHint("Confirm New Password");
        confirmPassInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
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

    public static Intent intentFactory(Context context) {
        return new Intent(context, AdminCheckActivity.class);
    }
}
