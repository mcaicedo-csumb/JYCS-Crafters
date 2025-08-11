package com.stanissudo.jycs_crafters.viewHolders;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.stanissudo.jycs_crafters.database.entities.FuelEntry;
import com.stanissudo.jycs_crafters.database.entities.Vehicle;

import java.util.Objects;

/**
 * @author Ysabelle Kim
 * created: 8/11/2025 - 1:48 AM
 * @version VERSION
 * Explanation:
 * @project JYCS-Crafters
 * @name VehicleAdapter.java
 */
public class VehicleAdapter extends ListAdapter<Vehicle, VehicleViewHolder> {

    public VehicleAdapter() {
        super(DIFF);
    }

    private static final DiffUtil.ItemCallback<Vehicle> DIFF =
            new DiffUtil.ItemCallback<>() {
                @Override public boolean areItemsTheSame(@NonNull Vehicle a, @NonNull Vehicle b) {
                    return a.getVehicleID() == b.getVehicleID();   // PK
                }
                @Override public boolean areContentsTheSame(@NonNull Vehicle a, @NonNull Vehicle b) {
                    return Objects.equals(a.getName(), b.getName())
                            && Objects.equals(a.getMake(), b.getMake())
                            && Objects.equals(a.getModel(), b.getModel())
                            && Objects.equals(a.getYear(), b.getYear());
                }
            };

    public VehicleAdapter(@NonNull DiffUtil.ItemCallback<Vehicle> diffCallback) {
        super(diffCallback);
    }

    @NonNull
    @Override
    public VehicleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return VehicleViewHolder.create(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull VehicleViewHolder holder, int position) {
        Vehicle current = getItem(position);
        holder.bind(current.toString());
    }

    public static class VehicleDiff extends DiffUtil.ItemCallback<Vehicle> {
        @Override
        public boolean areItemsTheSame(@NonNull Vehicle oldItem, @NonNull Vehicle newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Vehicle oldItem, @NonNull Vehicle newItem) {
            return oldItem.equals(newItem);
        }
    }
}
