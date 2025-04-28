package com.example.questapplication;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class CommunitiesPagerAdapter extends FragmentStateAdapter {

    public CommunitiesPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new CreateJoinCommunitiesFragment(); // Fragment for "Create or Join Communities"
            case 1:
                return new MyCommunitiesFragment(); // Fragment for "My Communities"
            default:
                throw new IllegalStateException("Unexpected position: " + position);
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Two tabs
    }
}
