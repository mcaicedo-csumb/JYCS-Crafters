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
 * @version VERSION
 * Explanation:
 * @project JYCS-Crafters
 * @name VehicleViewHolder.java
 */
public class GarageViewHolder extends RecyclerView.ViewHolder {

    private final TextView garageViewItem;

    private GarageViewHolder(View vehicleView) {
        super(vehicleView);
        garageViewItem = vehicleView.findViewById(R.id.garageActivityRecyclerItemTextView);
    }

    public void bind(String text) {
        garageViewItem.setText(text);
    }

    static GarageViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.garage_activity_recycler_item, parent, false);
        return new GarageViewHolder(view);
    }
}
