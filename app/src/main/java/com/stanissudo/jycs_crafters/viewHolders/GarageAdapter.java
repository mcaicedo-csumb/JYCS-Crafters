package com.stanissudo.jycs_crafters.viewHolders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.stanissudo.jycs_crafters.R;
import com.stanissudo.jycs_crafters.database.entities.Vehicle;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author Ysabelle Kim
 * created: 8/11/2025 - 1:48 AM
 * @version VERSION
 * Explanation:
 * @project JYCS-Crafters
 * @name VehicleAdapter.java
 */
public class GarageAdapter extends RecyclerView.Adapter<GarageAdapter.VH> {

    /** Interaction hooks for row actions. */
    public interface Callbacks {
        void onDeleteClicked(long id);
        void onEditClicked(long id);
    }

    /** Internal storage for items currently displayed. */
    private final List<Vehicle> items = new ArrayList<>();

    /** Receiver for row action events. */
    private final Callbacks callbacks;

    /**
     * Create the adapter.
     * @param callbacks Non-null callbacks for edit/delete actions.
     */
    public GarageAdapter(@NonNull Callbacks callbacks) {
        this.callbacks = callbacks;
        setHasStableIds(true);
    }

    /**
     * Replace the current list of items.
     * @param newItems New data (null treated as empty)
     */
    public void submitList(List<Vehicle> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.garage_activity_recycler_item, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull GarageAdapter.VH h, int position) {
        Vehicle e = items.get(position);

        // Texts
        h.vehicleNameText.setText(e.getName());
        h.makeText.setText(e.getMake());
        h.modelText.setText(e.getModel());
        h.yearText.setText(e.getYear());
        h.vehicleDetailsText.setText(buildDetails(e));

        // Actions
        long id = getStableId(e);
        h.btnVehicleDelete.setOnClickListener(v -> callbacks.onDeleteClicked(id));
        h.btnVehicleEdit.setOnClickListener(v -> callbacks.onEditClicked(id));
    }

    /**
     * Provide a stable item id derived from the entity's primary key.
     */
    @Override
    public long getItemId(int position) {
        return getStableId(items.get(position));
    }


    @Override
    public int getItemCount() { return items.size(); }

    // --------------------------------------------------------------------------------------------
    // ViewHolder
    // --------------------------------------------------------------------------------------------

    /** Simple holder that caches row view references. */
    static class VH extends RecyclerView.ViewHolder {
        final TextView vehicleNameText;
        final TextView makeText;
        final TextView modelText;
        final TextView yearText;
        final TextView vehicleDetailsText;
        final ImageButton btnVehicleDelete;
        final ImageButton btnVehicleEdit;
        VH(View v) {
            super(v);
            vehicleNameText     = v.findViewById(R.id.vehicleNameText);
            makeText = v.findViewById(R.id.makeText);
            modelText   = v.findViewById(R.id.modelText);
            yearText   = v.findViewById(R.id.yearText);
            vehicleDetailsText = v.findViewById(R.id.vehicleDetailsText);
            btnVehicleDelete    = v.findViewById(R.id.btnVehicleDelete);
            btnVehicleEdit      = v.findViewById(R.id.btnVehicleEdit);
        }
    }

    // --------------------------------------------------------------------------------------------
    // Formatting helpers
    // --------------------------------------------------------------------------------------------

    /**
     * Return a stable long id for RecyclerView animations (uses entity vehicleID).
     */
    private long getStableId(Vehicle e) {
        return e.getVehicleID();
    }

    /**
     * Build a compact details line
     */
    private String buildDetails(Vehicle e) {
        return String.format(
                Locale.US,
                "Make: %s • Model: %s • Year: %s",
                e.getMake(), e.getModel(), e.getYear()
        );
    }
}
