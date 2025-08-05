package com.stanissudo.jycs_crafters.viewHolders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.stanissudo.jycs_crafters.R;

/**
 * @author Ysabelle Kim
 * created: 8/4/2025 - 10:35 PM
 * @project JYCS-Crafters
 * file: FuelEntryViewHolder.java
 * @since 1.0.0
 * Explanation: FuelEntryViewHolder populates the recycler
 */
public class FuelEntryViewHolder extends RecyclerView.ViewHolder {

    private final TextView fuelEntryViewItem;

    private FuelEntryViewHolder(View fuelEntryView) {
        super(fuelEntryView);
        fuelEntryViewItem = fuelEntryView.findViewById(R.id.fuelEntryRecyclerItemTextView);
    }

    public void bind(String text) {
        fuelEntryViewItem.setText(text);
    }

    static FuelEntryViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fuelentry_recycler_item, parent, false);
        return new FuelEntryViewHolder(view);
    }
}
