package com.stanissudo.jycs_crafters;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.stanissudo.jycs_crafters.database.FuelTrackAppRepository;

public class SettingsActivity extends AppCompatActivity {

    private FuelTrackAppRepository repository;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        repository = FuelTrackAppRepository.getRepository(getApplication());

        Button changeBtn = findViewById(R.id.changePasswordButton);
        changeBtn.setOnClickListener(v -> showChangePasswordDialog());
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

            SharedPreferences sp = getSharedPreferences("login_prefs", Context.MODE_PRIVATE);
            String currentUsername = sp.getString("username", "");

            repository.changePasswordWithCurrentCheck(currentUsername, cur, np, (ok, msg) -> {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            });
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    // Added so MainActivity can launch Settings with SettingsActivity.intentFactory(this)
    public static android.content.Intent intentFactory(Context context) {
        return new android.content.Intent(context, SettingsActivity.class);
    }
}
