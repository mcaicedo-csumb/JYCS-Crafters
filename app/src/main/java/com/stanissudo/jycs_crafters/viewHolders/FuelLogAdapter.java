package com.stanissudo.jycs_crafters.viewHolders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.stanissudo.jycs_crafters.R;
import com.stanissudo.jycs_crafters.database.entities.FuelEntry;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

//public class FuelLogAdapter extends ListAdapter<FuelEntry, FuelLogAdapter.VH> {
//    public FuelLogAdapter() {
//        super(DIFF);
//    }
//
//    private static final DiffUtil.ItemCallback<FuelEntry> DIFF =
//            new DiffUtil.ItemCallback<>() {
//                @Override public boolean areItemsTheSame(@NonNull FuelEntry a, @NonNull FuelEntry b) {
//                    return a.getLogID() == b.getLogID();   // PK
//                }
//                @Override public boolean areContentsTheSame(@NonNull FuelEntry a, @NonNull FuelEntry b) {
//                    return a.getOdometer().equals(b.getOdometer())
//                            && Double.compare(a.getGallons(), b.getGallons()) == 0
//                            && Double.compare(a.getPricePerGallon(), b.getPricePerGallon()) == 0
//                            && Objects.equals(a.getLogDate(), b.getLogDate());
//                }
//            };
//
//    static class VH extends RecyclerView.ViewHolder {
//        TextView dateText, detailText;
//        VH(View v) {
//            super(v);
//            dateText = v.findViewById(R.id.dateText);
//            detailText = v.findViewById(R.id.detailText);
//        }
//    }
//
//    @NonNull @Override
//    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View v = LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.item_fuel_entry, parent, false);
//        return new VH(v);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull VH h, int pos) {
//        FuelEntry e = getItem(pos);
//
//        // If you store LocalDateTime (converted by your TypeConverter):
//        LocalDateTime dt = e.getLogDate();
//        String when = dt.format(DateTimeFormatter.ofPattern("MM/dd/yyyy h:mm a", Locale.US));
//
//        h.dateText.setText(when);
//        h.detailText.setText(
//                String.format(Locale.US, "Odometer: %d • Gas: %.2f gal • $/gal: %.3f",
//                        e.getOdometer(), e.getGallons(), e.getPricePerGallon())
//        );
//    }
//}

public class FuelLogAdapter extends RecyclerView.Adapter<FuelLogAdapter.VH> {

    public interface Callbacks {
        void onDeleteClicked(long id);
        void onEditClicked(long id);
    }

    private final List<FuelEntry> items = new ArrayList<>();
    private final Callbacks callbacks;

    public FuelLogAdapter(Callbacks callbacks) { this.callbacks = callbacks; }

    public void submitList(List<FuelEntry> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fuelentry_recycler_item, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        FuelEntry e = items.get(position);

        // Bind texts (adjust to your model fields)
        h.dateText.setText(formatDate(e));          // implement below to suit your model
        h.detailText.setText(buildDetails(e));      // gallons/price/odo, etc.

        long id = getId(e); // <- uses your LogID getter
        h.btnDelete.setOnClickListener(v -> callbacks.onDeleteClicked(id));
        h.btnEdit.setOnClickListener(v -> callbacks.onEditClicked(id));
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView dateText, detailText;
        ImageButton btnDelete, btnEdit;
        VH(View v) {
            super(v);
            dateText  = v.findViewById(R.id.dateText);
            detailText= v.findViewById(R.id.detailText);
            btnDelete = v.findViewById(R.id.btnDelete);
            btnEdit   = v.findViewById(R.id.btnEdit);
        }
    }

    // ---- helpers you can tailor to your entity ----
    private long getId(FuelEntry e) {
        // If your entity has LogID (int):
        return e.getLogID(); // or (long) e.getLogID();
        // If your getter is getId(), then just return e.getId();
    }

    private String formatDate(FuelEntry e) {
        // Example: if you store millis -> format; if you store a String -> return it.
        // return DateFormat.getDateInstance().format(new Date(e.getDateMillis()));
        LocalDateTime dt = e.getLogDate();
        return dt.format(DateTimeFormatter.ofPattern("MM/dd/yyyy h:mm a", Locale.US));
    }

    private String buildDetails(FuelEntry e) {
        // Example composing details:
        // return String.format(Locale.US, "%.2f gal @ $%.2f • Odo %d", e.getGallons(), e.getPricePerGal(), e.getOdometer());
        //return e.getSummary(); // or compose as needed
        //        LocalDateTime dt = e.getLogDate();



        return
                String.format(Locale.US, "Odometer: %d • Gas: %.2f gal • $/gal: %.3f",
                        e.getOdometer(), e.getGallons(), e.getPricePerGallon());
    }
}
