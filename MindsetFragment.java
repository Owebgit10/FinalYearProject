package com.example.questapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MindsetFragment extends Fragment {

    private ViewPager2 viewPagerMindset;
    private TabLayout tabLayoutMindset;

    public MindsetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mindset, container, false);

        tabLayoutMindset = view.findViewById(R.id.mindsetTabLayout);
        viewPagerMindset = view.findViewById(R.id.mindsetViewPager);

        // Set up ViewPager2 adapter
        MindsetPagerAdapter adapter = new MindsetPagerAdapter(this);
        viewPagerMindset.setAdapter(adapter);

        // Link TabLayout with ViewPager2
        new TabLayoutMediator(tabLayoutMindset, viewPagerMindset,
                (tab, position) -> {
                    if (position == 0) tab.setText("Get Inspired");
                    else if (position == 1) tab.setText("Quote Book");
                }).attach();

        // Optional: make sure it lands on Get Inspired (position 0)
        viewPagerMindset.setCurrentItem(0, false);

        return view;
    }
}

