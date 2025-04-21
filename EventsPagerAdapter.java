package com.example.questapplication;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class EventsPagerAdapter extends FragmentStateAdapter {

    public EventsPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new CreateJoinEventsFragment(); // Fragment for "Create or Join Events"
            case 1:
                return new ManageEventsFragment(); // Fragment for "Manage Events"
            default:
                throw new IllegalStateException("Unexpected position: " + position);
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Two tabs
    }
}

