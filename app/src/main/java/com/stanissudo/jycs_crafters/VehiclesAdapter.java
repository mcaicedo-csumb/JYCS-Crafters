package com.stanissudo.jycs_crafters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.stanissudo.jycs_crafters.database.entities.Vehicle;

import java.util.ArrayList;
import java.util.List;

/** Camila: simple ListView adapter for admin vehicle review */
public class VehiclesAdapter extends BaseAdapter {

    interface Callbacks { void onSoftDelete(Vehicle v); void onRestore(Vehicle v); void onHardDelete(Vehicle v); }

    private final LayoutInflater inflater;
    private final Callbacks cb;
    private final List<Vehicle> items = new ArrayList<>();
    private boolean listIsInactive = true;

    public VehiclesAdapter(Context ctx, List<Vehicle> initial, Callbacks cb) {
        this.inflater = LayoutInflater.from(ctx);
        this.cb = cb;
        if (initial != null) items.addAll(initial);
    }

    public void setItems(List<Vehicle> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    public void setListIsInactive(boolean inactive) { this.listIsInactive = inactive; }

    @Override public int getCount() { return items.size(); }
    @Override public Vehicle getItem(int position) { return items.get(position); }
    @Override public long getItemId(int position) { return items.get(position).getId(); }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        Holder h;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_vehicle_row, parent, false);
            h = new Holder(convertView);
            convertView.setTag(h);
        } else {
            h = (Holder) convertView.getTag();
        }

        Vehicle v = getItem(position);
        h.title.setText(v.getName() == null ? ("Vehicle #" + v.getId()) : v.getName());
        h.subtitle.setText("userId=" + v.getUserId() + (v.isActive() ? " • active" : " • inactive"));

        if (listIsInactive) {
            h.btnPrimary.setText("Restore");
            h.btnSecondary.setText("Delete Permanently");
            h.btnPrimary.setOnClickListener(view -> cb.onRestore(v));
            h.btnSecondary.setOnClickListener(view -> cb.onHardDelete(v));
        } else {
            h.btnPrimary.setText("Soft Delete");
            h.btnSecondary.setText("—");
            h.btnSecondary.setEnabled(false);
            h.btnPrimary.setOnClickListener(view -> cb.onSoftDelete(v));
            h.btnSecondary.setOnClickListener(null);
        }

        return convertView;
    }

    static class Holder {
        final TextView title, subtitle;
        final Button btnPrimary, btnSecondary;
        Holder(View root) {
            title = root.findViewById(R.id.txtTitle);
            subtitle = root.findViewById(R.id.txtSubtitle);
            btnPrimary = root.findViewById(R.id.btnPrimary);
            btnSecondary = root.findViewById(R.id.btnSecondary);
        }
    }
}
