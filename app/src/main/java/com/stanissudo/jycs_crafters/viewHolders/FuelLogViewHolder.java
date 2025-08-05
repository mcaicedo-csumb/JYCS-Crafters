package com.stanissudo.jycs_crafters.viewHolders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.stanissudo.jycs_crafters.R;

/**
 * @author Ysabelle Kim
 * created: 8/4/2025 - 8:10 PM
 * @project JYCS-Crafters
 * file: FuelLogViewHolder.java
 * @since 1.0.0
 * Explanation: populates the recycler
 */
public class FuelLogViewHolder extends RecyclerView.ViewHolder {
    private final TextView fuelLogViewItem;

    private FuelLogViewHolder(View fuelLogView) {
        super(fuelLogView);
        fuelLogViewItem = fuelLogView.findViewById(R.id.recyclerItemTextView);
    }

    public void bind(String text) {
        fuelLogViewItem.setText(text);
    }

    static FuelLogViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fuel_log_recycler_item, parent, false);
        return new FuelLogViewHolder(view);
    }
}
