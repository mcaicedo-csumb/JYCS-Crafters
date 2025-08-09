package com.stanissudo.jycs_crafters.viewHolders;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.stanissudo.jycs_crafters.database.entities.FuelEntry;

/**
 * @author Ysabelle Kim
 * created: 8/4/2025 - 10:35 PM
 * @project JYCS-Crafters
 * file: FuelEntryAdapter.java
 * @since 1.0.0
 * Explanation: creates recycler widget and binds it to MainActivity
 */
public class FuelEntryAdapter extends ListAdapter<FuelEntry, FuelEntryViewHolder> {

    public FuelEntryAdapter(@NonNull DiffUtil.ItemCallback<FuelEntry> diffCallback) {
        super(diffCallback);
    }

    @NonNull
    @Override
    public FuelEntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return FuelEntryViewHolder.create(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull FuelEntryViewHolder holder, int position) {
        FuelEntry current = getItem(position);
        holder.bind(current.toString());
    }

    public static class FuelEntryDiff extends DiffUtil.ItemCallback<FuelEntry> {
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
