package com.example.questapplication;



import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class CommunitiesFragment extends Fragment {

    private ViewPager2 viewPagerCommunities;
    private TabLayout tabLayoutCommunities;

    public CommunitiesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_communities, container, false);

        tabLayoutCommunities = view.findViewById(R.id.communitiesTabLayout);
        viewPagerCommunities = view.findViewById(R.id.communitiesViewPager);

        // Set up ViewPager2 adapter
        CommunitiesPagerAdapter adapter = new CommunitiesPagerAdapter(this);
        viewPagerCommunities.setAdapter(adapter);

        // Link TabLayout with ViewPager2
        new TabLayoutMediator(tabLayoutCommunities, viewPagerCommunities,
                (tab, position) -> {
                    if (position == 0) tab.setText("Create or Join Communities");
                    else if (position == 1) tab.setText("My Communities");
                }).attach();

        // Optional: default to the first tab
        viewPagerCommunities.setCurrentItem(0, false);

        return view;
    }
}
