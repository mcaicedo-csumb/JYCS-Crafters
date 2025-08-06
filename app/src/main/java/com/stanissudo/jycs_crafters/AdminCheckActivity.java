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

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.addView(usernameInput);
        layout.addView(passwordInput);
        builder.setView(layout);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (!username.isEmpty() && !password.isEmpty()) {
                User newUser = new User(username, password);
                repository.insertUser(newUser);
                Toast.makeText(this, "User added", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
            }
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

            if (!username.isEmpty()) {
                repository.deleteUserByUsername(username);
                Toast.makeText(this, "User removed", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Password");

        EditText usernameInput = new EditText(this);
        usernameInput.setHint("Username");
        EditText newPasswordInput = new EditText(this);
        newPasswordInput.setHint("New Password");

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.addView(usernameInput);
        layout.addView(newPasswordInput);
        builder.setView(layout);

        builder.setPositiveButton("Change", (dialog, which) -> {
            String username = usernameInput.getText().toString().trim();
            String newPassword = newPasswordInput.getText().toString().trim();

            if (!username.isEmpty() && !newPassword.isEmpty()) {
                repository.updatePassword(username, newPassword);
                Toast.makeText(this, "Password changed", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
            }
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
