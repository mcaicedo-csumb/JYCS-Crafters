package com.stanissudo.jycs_crafters.viewHolders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.stanissudo.jycs_crafters.R;

/**
 * @author Ysabelle Kim
 * created: 8/11/2025 - 1:49 AM
 * Explanation: <p>VehicleViewHolder binds the recycler view to the activity.</p>
 * @project JYCS-Crafters
 * @name VehicleViewHolder.java
 */
public class VehicleViewHolder extends RecyclerView.ViewHolder {

    private final TextView vehicleViewItem;

    private VehicleViewHolder(View vehicleView) {
        super(vehicleView);
        vehicleViewItem = vehicleView.findViewById(R.id.garageDisplayRecyclerView);
    }

    public void bind(String text) {
        vehicleViewItem.setText(text);
    }

    static VehicleViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.garage_activity_recycler_item, parent, false);
        return new VehicleViewHolder(view);
    }
}
