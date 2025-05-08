package com.example.questapplication;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class GoalsPagerAdapter extends FragmentStateAdapter {

    public GoalsPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new MyGoalsFragment(); // Fragment for "My Goals"
            case 1:
                return new OverallStatsFragment(); // Fragment for "Overall Stats"
            default:
                throw new IllegalStateException("Unexpected position: " + position);
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Two tabs
    }
}

