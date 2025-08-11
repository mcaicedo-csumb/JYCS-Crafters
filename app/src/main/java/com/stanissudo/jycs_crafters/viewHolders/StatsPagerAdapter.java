package com.stanissudo.jycs_crafters.viewHolders;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.stanissudo.jycs_crafters.fragments.CostStatsFragment;
import com.stanissudo.jycs_crafters.fragments.DistanceStatsFragment;

public class StatsPagerAdapter extends FragmentStateAdapter {

    public StatsPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Return a new fragment instance for the given position
        if (position == 1) {
            return new DistanceStatsFragment();
        }
        return new CostStatsFragment();
    }

    @Override
    public int getItemCount() {
        return 2; // We have 2 tabs
    }
}
