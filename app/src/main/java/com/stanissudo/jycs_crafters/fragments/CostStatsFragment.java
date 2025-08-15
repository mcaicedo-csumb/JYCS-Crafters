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
import com.stanissudo.jycs_crafters.viewHolders.CostStatsViewModel;
import com.stanissudo.jycs_crafters.viewHolders.SharedViewModel;

import java.util.Locale;

/**
 * * @author Stan Permiakov
 * * created: 8/12/2025
 * * @project JYCS-Crafters
 */
public class CostStatsFragment extends Fragment {

    private CostStatsViewModel costStatsViewModel;
    private TextView totalCostText, avgPriceText, avgFillUpText, totalFillUpsText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cost_stats, container, false);
        totalFillUpsText = view.findViewById(R.id.total_fill_ups_value);
        totalCostText = view.findViewById(R.id.total_cost_value);
        avgPriceText = view.findViewById(R.id.avg_price_value);
        avgFillUpText = view.findViewById(R.id.avg_fillup_cost_value);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        costStatsViewModel = new ViewModelProvider(this).get(CostStatsViewModel.class);

        // Get the ViewModel that is scoped to the MainActivity
        SharedViewModel sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // 1) Observe the reactive stats stream ONCE
        costStatsViewModel.stats.observe(getViewLifecycleOwner(), stats -> {
            if (stats != null) {
                totalFillUpsText.setText(String.format(Locale.US, "%d", stats.fillUpsCount));
                totalCostText.setText(String.format(Locale.US, "$%.2f", stats.totalCost));
                avgPriceText.setText(String.format(Locale.US, "$%.2f", stats.avgPricePerGallon));
                avgFillUpText.setText(String.format(Locale.US, "$%.2f", stats.avgCostPerFillUp));
            } else {
                totalCostText.setText("$0.00");
                avgPriceText.setText("$0.00");
                avgFillUpText.setText("$0.00");
            }
        });

        // 2) Drive the ViewModel with selection changes
        sharedViewModel.getSelectedCarId().observe(getViewLifecycleOwner(),
                id -> {
                    if (id != null) costStatsViewModel.setVehicleId(id);
                });
    }
}