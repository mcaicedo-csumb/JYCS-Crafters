package com.stanissudo.jycs_crafters.viewHolders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.stanissudo.jycs_crafters.R;
import com.stanissudo.jycs_crafters.database.entities.FuelEntry;
import com.stanissudo.jycs_crafters.utils.NumberFormatter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * *  @author Stan Permiakov
 * *  created: 8/12/2025
 * *  @project JYCS-Crafters
 * *
 * RecyclerView adapter that renders {@link FuelEntry} rows with Edit/Delete actions.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Own a mutable list of {@link FuelEntry} items (updated via {@link #submitList(List)}).</li>
 *   <li>Bind date, odometer, and detail summary to the row views.</li>
 *   <li>Expose callbacks for Edit/Delete button clicks.</li>
 * </ul>
 * <p>
 * Notes:
 * <ul>
 *   <li>Adapter uses stable IDs so RecyclerView can animate changes predictably.</li>
 *   <li>For large lists or frequent updates, consider migrating to {@code ListAdapter}
 *       with {@code DiffUtil.ItemCallback}.</li>
 * </ul>
 */
public class FuelLogAdapter extends RecyclerView.Adapter<FuelLogAdapter.VH> {

    /** Interaction hooks for row actions. */
    public interface Callbacks {
        void onDeleteClicked(long id);
        void onEditClicked(long id);
    }

    /** Internal storage for items currently displayed. */
    private final List<FuelEntry> items = new ArrayList<>();

    /** Receiver for row action events. */
    private final Callbacks callbacks;

    /** Single shared formatter for date/time display. */
    private static final DateTimeFormatter DATE_TIME_FMT =
            DateTimeFormatter.ofPattern("MM/dd/yyyy h:mm a", Locale.US);

    /**
     * Create the adapter.
     * @param callbacks Non-null callbacks for edit/delete actions.
     */
    public FuelLogAdapter(@NonNull Callbacks callbacks) {
        this.callbacks = callbacks;
        setHasStableIds(true);
    }

    /**
     * Replace the current list of items.
     * @param newItems New data (null treated as empty)
     */
    public void submitList(List<FuelEntry> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fuelentry_recycler_item, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        FuelEntry e = items.get(position);

        // Texts
        h.dateText.setText(formatDate(e.getLogDate()));
        h.odometerText.setText(buildOdometer(e));
        h.detailText.setText(buildDetails(e));

        // Actions
        long id = getStableId(e);
        h.btnDelete.setOnClickListener(v -> callbacks.onDeleteClicked(id));
        h.btnEdit.setOnClickListener(v -> callbacks.onEditClicked(id));
    }

    @Override
    public int getItemCount() { return items.size(); }

    /**
     * Provide a stable item id derived from the entity's primary key.
     */
    @Override
    public long getItemId(int position) {
        return getStableId(items.get(position));
    }

    // --------------------------------------------------------------------------------------------
    // ViewHolder
    // --------------------------------------------------------------------------------------------

    /** Simple holder that caches row view references. */
    static class VH extends RecyclerView.ViewHolder {
        final TextView dateText;
        final TextView odometerText;
        final TextView detailText;
        final ImageButton btnDelete;
        final ImageButton btnEdit;
        VH(View v) {
            super(v);
            dateText     = v.findViewById(R.id.dateText);
            odometerText = v.findViewById(R.id.odometerText);
            detailText   = v.findViewById(R.id.detailText);
            btnDelete    = v.findViewById(R.id.btnDelete);
            btnEdit      = v.findViewById(R.id.btnEdit);
        }
    }

    // --------------------------------------------------------------------------------------------
    // Formatting helpers
    // --------------------------------------------------------------------------------------------

    /**
     * Return a stable long id for RecyclerView animations (uses entity LogID).
     */
    private long getStableId(FuelEntry e) {
        return e.getLogID();
    }

    /**
     * Format the entry date/time for display; returns empty string if null.
     */
    private String formatDate(LocalDateTime dt) {
        return dt == null ? "" : dt.format(DATE_TIME_FMT);
    }

    /**
     * Build the odometer label like: "Odometer: 123,456".
     */
    private String buildOdometer(FuelEntry e) {
        return String.format(Locale.US, "Odometer: %d", e.getOdometer());
    }

    /**
     * Build a compact details line like: "Gas: 12.345 gal • $/gal: 4.39 • Total: $54.25".
     */
    private String buildDetails(FuelEntry e) {
        return String.format(
                Locale.US,
                "Gas: %s gal • $/gal: %s • Total: $%s",
                NumberFormatter.upTo3(e.getGallons()),
                NumberFormatter.upTo2(e.getPricePerGallon()),
                NumberFormatter.upTo2(e.getTotalCost())
        );
    }
}
