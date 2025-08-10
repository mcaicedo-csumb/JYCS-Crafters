package com.stanissudo.jycs_crafters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.stanissudo.jycs_crafters.database.entities.User;

import java.util.List;

/** Camila: adapter for admin user management (toggle active/inactive) */
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.VH> {

    public interface OnUserToggle { void onToggle(User u, boolean activate); }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, subtitle;
        Button btnToggle;
        VH(@NonNull ViewGroup root) {
            super(LayoutInflater.from(root.getContext())
                    .inflate(R.layout.item_user_row, root, false));
            title = itemView.findViewById(R.id.txtTitle);
            subtitle = itemView.findViewById(R.id.txtSubtitle);
            btnToggle = itemView.findViewById(R.id.btnToggle);
        }
    }

    private List<User> data;
    private final OnUserToggle callback;

    public UserAdapter(List<User> data, OnUserToggle cb) { this.data = data; this.callback = cb; }
    public void submit(List<User> d) { this.data = d; notifyDataSetChanged(); }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { return new VH(parent); }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        User u = data.get(pos);
        h.title.setText(u.getUsername());
        String sub = "id=" + u.getId() + (u.isAdmin() ? " • admin" : "") + (u.isActive() ? " • active" : " • inactive");
        h.subtitle.setText(sub);
        h.btnToggle.setText(u.isActive() ? "Deactivate" : "Reactivate");
        h.btnToggle.setOnClickListener(v -> callback.onToggle(u, !u.isActive()));
    }

    @Override public int getItemCount() { return data == null ? 0 : data.size(); }
}
