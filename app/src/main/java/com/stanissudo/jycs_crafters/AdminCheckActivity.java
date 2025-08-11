package com.stanissudo.jycs_crafters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.stanissudo.jycs_crafters.database.FuelTrackAppRepository;
import com.stanissudo.jycs_crafters.database.entities.User;
import com.stanissudo.jycs_crafters.databinding.ActivityAdminCheckBinding;

public class AdminCheckActivity extends AppCompatActivity {

    private ActivityAdminCheckBinding binding;
    private FuelTrackAppRepository repository;
    private SharedPreferences sharedPreferences;
    private UserListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminCheckBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sharedPreferences = getSharedPreferences("login_prefs", MODE_PRIVATE);
        repository = FuelTrackAppRepository.getRepository(getApplication());

        binding.adminMessage.setText("You have admin privileges!");

        // Set up RecyclerView
        adapter = new UserListAdapter();
        binding.userListRecycler.setLayoutManager(new LinearLayoutManager(this));
        binding.userListRecycler.setAdapter(adapter);

        // Observe the user list
        repository.getAllUsers().observe(this, users -> adapter.setUsers(users));

        // Buttons
        binding.addUserButton.setOnClickListener(v -> showAddUserDialog());
        binding.removeUserButton.setOnClickListener(v -> showRemoveUserDialog());
        binding.changePasswordButton.setOnClickListener(v -> showChangePasswordDialog());
        binding.logoutButton.setOnClickListener(v -> logout());
    }

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

            // ✅ Uses repository-level duplicate guard
            repository.addUserSafely(username, password, /*isAdmin=*/false, (ok, msg) -> {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                // If your list isn't LiveData, refresh here.
            });
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showRemoveUserDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Remove User");

        android.widget.EditText usernameInput = new android.widget.EditText(this);
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

    // 🔒 Updated: requires CURRENT password and calls changePasswordWithCurrentCheck(...)
    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Password");

        EditText currentPassInput = new EditText(this);
        currentPassInput.setHint("Current Password");
        currentPassInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

        EditText newPassInput = new EditText(this);
        newPassInput.setHint("New Password");
        newPassInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

        EditText confirmPassInput = new EditText(this);
        confirmPassInput.setHint("Confirm New Password");
        confirmPassInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.addView(currentPassInput);
        layout.addView(newPassInput);
        layout.addView(confirmPassInput);
        builder.setView(layout);

        builder.setPositiveButton("Change", (dialog, which) -> {
            String cur = currentPassInput.getText().toString().trim();
            String np = newPassInput.getText().toString().trim();
            String cp = confirmPassInput.getText().toString().trim();

            if (cur.isEmpty() || np.isEmpty() || cp.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!np.equals(cp)) {
                Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences sp = getSharedPreferences("login_prefs", Context.MODE_PRIVATE);
            String currentUsername = sp.getString("username", "");

            repository.changePasswordWithCurrentCheck(currentUsername, cur, np, (ok, msg) -> {
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
