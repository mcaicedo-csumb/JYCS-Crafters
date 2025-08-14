package com.stanissudo.jycs_crafters.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.stanissudo.jycs_crafters.R;
import com.stanissudo.jycs_crafters.viewHolders.DistanceStatsViewModel;
import com.stanissudo.jycs_crafters.viewHolders.SharedViewModel;

import java.util.Locale;

public class DistanceStatsFragment extends Fragment {

    private DistanceStatsViewModel distanceStatsViewModel;
    private TextView lastOdometer, totalDistance, avgDistancePerFillUp;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_distance_stats, container, false);
        lastOdometer = view.findViewById(R.id.last_odo_value);
        totalDistance = view.findViewById(R.id.total_distance_value);
        avgDistancePerFillUp = view.findViewById(R.id.avg_distance_value);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        distanceStatsViewModel = new ViewModelProvider(this).get(DistanceStatsViewModel.class);

        // Get the ViewModel that is scoped to the MainActivity
        SharedViewModel sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // 1) Observe the reactive stats stream ONCE
        distanceStatsViewModel.stats.observe(getViewLifecycleOwner(), stats -> {
            if (stats != null) {
                lastOdometer.setText(String.format(Locale.US, "%d", stats.lastOdometer));
                totalDistance.setText(String.format(Locale.US, "%d", stats.totalDistance));
                avgDistancePerFillUp.setText(String.format(Locale.US, "%.2f", stats.avgDistancePerFillUp));
            } else {
                lastOdometer.setText("0");
                totalDistance.setText("0");
                avgDistancePerFillUp.setText("0.00");
            }
        });

        // 2) Drive the ViewModel with selection changes
        sharedViewModel.getSelectedCarId().observe(getViewLifecycleOwner(),
                id -> {
                    if (id != null) distanceStatsViewModel.setVehicleId(id);
                });
    }
}