package com.stanissudo.jycs_crafters;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.stanissudo.jycs_crafters.database.FuelTrackAdminRepository;
import com.stanissudo.jycs_crafters.database.entities.User;

import java.util.ArrayList;
import java.util.List;

/** Camila: Admin screen to deactivate/reactivate users */
public class UserManagementActivity extends AppCompatActivity {

    private FuelTrackAdminRepository repo;
    private UserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management);

        repo = new FuelTrackAdminRepository(getApplication());

        RecyclerView rv = findViewById(R.id.recyclerUsers);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        adapter = new UserAdapter(new ArrayList<>(), (user, activate) -> {
            repo.setUserActive(user.getId(), activate);
            Toast.makeText(this,
                    (activate ? "Reactivated " : "Deactivated ") + user.getUsername(),
                    Toast.LENGTH_SHORT).show();
        });
        rv.setAdapter(adapter);

        repo.getAllUsers().observe(this, new Observer<List<User>>() {
            @Override public void onChanged(List<User> users) {
                adapter.submit(users);
            }
        });
    }
}