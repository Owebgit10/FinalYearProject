package com.example.questapplication;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;


public class MindsetPagerAdapter extends FragmentStateAdapter {

    public MindsetPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new GetInspiredFragment();
        } else {
            return new QuoteBookFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Two tabs
    }
}

