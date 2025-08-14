/**
 * Author: Jose Caicedo
 * Date: 08/05/2025
 *
 * RecyclerView adapter for displaying a list of {@link com.stanissudo.jycs_crafters.database.entities.User} objects.
 * This adapter binds user data to a simple two-line list item layout:
 * - Line 1: Username
 * - Line 2: Role ("Admin" or "User")
 *
 * Usage:
 * - Call {@link #setUsers(List)} to provide or update the dataset.
 * - Attach this adapter to a RecyclerView with a suitable LayoutManager (e.g., LinearLayoutManager).
 *
 * Notes:
 * - Uses the built-in Android layout {@code android.R.layout.simple_list_item_2}.
 * - Calls {@link #notifyDataSetChanged()} after updating the dataset for simplicity.
 *   For large datasets, consider using {@link androidx.recyclerview.widget.DiffUtil} for better performance.
 */
package com.stanissudo.jycs_crafters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.stanissudo.jycs_crafters.database.entities.User;

import java.util.List;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserViewHolder> {

    /** The list of users to display in the RecyclerView. */
    private List<User> users;

    /**
     * Updates the adapter's dataset with a new list of users and refreshes the UI.
     *
     * @param users A list of {@link User} objects to display.
     */
    public void setUsers(List<User> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    /**
     * Creates a new {@link UserViewHolder} when there are no existing holders to reuse.
     *
     * @param parent   The parent ViewGroup into which the new View will be added.
     * @param viewType The type of the new view (not used here as there's only one type).
     * @return A new {@link UserViewHolder} instance.
     */
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new UserViewHolder(view);
    }

    /**
     * Binds user data to the views in the {@link UserViewHolder}.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the item.
     * @param position The position of the item within the adapter's dataset.
     */
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.usernameView.setText(user.getUsername());
        holder.adminView.setText(user.isAdmin() ? "Admin" : "User");
    }

    /**
     * Returns the total number of items in the dataset.
     *
     * @return The number of user items, or 0 if the dataset is null.
     */
    @Override
    public int getItemCount() {
        return users == null ? 0 : users.size();
    }

    /**
     * ViewHolder class for holding references to the username and role TextViews.
     */
    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView usernameView, adminView;

        /**
         * Constructs a new ViewHolder and retrieves references to the relevant views.
         *
         * @param itemView The inflated layout view for the list item.
         */
        UserViewHolder(View itemView) {
            super(itemView);
            usernameView = itemView.findViewById(android.R.id.text1);
            adminView = itemView.findViewById(android.R.id.text2);
        }
    }
}
