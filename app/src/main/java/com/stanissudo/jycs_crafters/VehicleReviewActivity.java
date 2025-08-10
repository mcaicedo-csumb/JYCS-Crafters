package com.stanissudo.jycs_crafters;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.stanissudo.jycs_crafters.database.FuelTrackAdminRepository;
import com.stanissudo.jycs_crafters.database.entities.Vehicle;

import java.util.ArrayList;

/** Camila: Admin screen to review vehicles (soft-delete/restore/hard delete) */
public class VehicleReviewActivity extends AppCompatActivity {

    private FuelTrackAdminRepository repo;
    private VehiclesAdapter adapter;
    private TextView emptyText;
    private boolean showingInactive = true; // default to inactive list first

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_review);

        repo = new FuelTrackAdminRepository(getApplication());

        ListView list = findViewById(R.id.vehicleList);
        emptyText = findViewById(R.id.emptyText);
        Button btnInactive = findViewById(R.id.btnShowInactive);
        Button btnActive = findViewById(R.id.btnShowActive);

        adapter = new VehiclesAdapter(this, new ArrayList<>(), new VehiclesAdapter.Callbacks() {
            @Override public void onSoftDelete(Vehicle v) { repo.softDeleteVehicle(v.getId()); }
            @Override public void onRestore(Vehicle v)     { repo.restoreVehicle(v.getId()); }
            @Override public void onHardDelete(Vehicle v)  { repo.deleteVehicle(v); }
        });
        list.setAdapter(adapter);

        btnInactive.setOnClickListener(v -> { showingInactive = true; hookLiveData(); });
        btnActive.setOnClickListener(v ->   { showingInactive = false; hookLiveData(); });

        hookLiveData();
    }

    private void hookLiveData() {
        adapter.setListIsInactive(showingInactive);
        if (showingInactive) {
            repo.getInactiveVehicles().observe(this, vehicles -> {
                adapter.setItems(vehicles);
                emptyText.setVisibility((vehicles == null || vehicles.isEmpty()) ? View.VISIBLE : View.GONE);
            });
        } else {
            repo.getActiveVehicles().observe(this, vehicles -> {
                adapter.setItems(vehicles);
                emptyText.setVisibility((vehicles == null || vehicles.isEmpty()) ? View.VISIBLE : View.GONE);
            });
        }
    }
}
