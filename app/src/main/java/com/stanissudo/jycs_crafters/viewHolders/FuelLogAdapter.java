package com.stanissudo.jycs_crafters.viewHolders;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.stanissudo.jycs_crafters.database.entities.FuelEntry;

/**
 * @author Ysabelle Kim
 * created: 8/4/2025 - 8:09 PM
 * @project JYCS-Crafters
 * file: FuelLogAdapter.java
 * @since 1.0.0
 * Explanation: creates recycler widget and binds it to MainActivity
 */
public class FuelLogAdapter extends ListAdapter<FuelEntry, FuelLogViewHolder> {

    public FuelLogAdapter(@NonNull DiffUtil.ItemCallback<FuelEntry> diffCallback) {
        super(diffCallback);
    }

    @NonNull
    @Override
    public FuelLogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return FuelLogViewHolder.create(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull FuelLogViewHolder holder, int position) {
        FuelEntry current = getItem(position);
        holder.bind(current.toString());
    }

    public static class FuelLogDiff extends DiffUtil.ItemCallback<FuelEntry> {
        @Override
        public boolean areItemsTheSame(@NonNull FuelEntry oldItem, @NonNull FuelEntry newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull FuelEntry oldItem, @NonNull FuelEntry newItem) {
            return oldItem.equals(newItem);
        }
    }
}
