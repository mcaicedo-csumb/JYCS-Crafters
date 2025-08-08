package com.stanissudo.jycs_crafters.viewHolders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.stanissudo.jycs_crafters.R;
import com.stanissudo.jycs_crafters.database.entities.FuelEntry;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

public class FuelLogAdapter extends ListAdapter<FuelEntry, FuelLogAdapter.VH> {
    public FuelLogAdapter() {
        super(DIFF);
    }

    private static final DiffUtil.ItemCallback<FuelEntry> DIFF =
            new DiffUtil.ItemCallback<>() {
                @Override public boolean areItemsTheSame(@NonNull FuelEntry a, @NonNull FuelEntry b) {
                    return a.getLogID() == b.getLogID();   // PK
                }
                @Override public boolean areContentsTheSame(@NonNull FuelEntry a, @NonNull FuelEntry b) {
                    return a.getOdometer() == b.getOdometer()
                            && Double.compare(a.getGallons(), b.getGallons()) == 0
                            && Double.compare(a.getPricePerGallon(), b.getPricePerGallon()) == 0
                            && Objects.equals(a.getLogDate(), b.getLogDate());
                }
            };

    static class VH extends RecyclerView.ViewHolder {
        TextView dateText, detailText;
        VH(View v) {
            super(v);
            dateText = v.findViewById(R.id.dateText);
            detailText = v.findViewById(R.id.detailText);
        }
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_fuel_entry, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        FuelEntry e = getItem(pos);

        // If you store LocalDateTime (converted by your TypeConverter):
        LocalDateTime dt = e.getLogDate();
        String when = dt.format(DateTimeFormatter.ofPattern("MM/dd/yyyy h:mm a", Locale.US));

        h.dateText.setText(when);
        h.detailText.setText(
                String.format(Locale.US, "Odometer: %d • Gas: %.2f gal • $/gal: %.3f",
                        e.getOdometer(), e.getGallons(), e.getPricePerGallon())
        );
    }
}