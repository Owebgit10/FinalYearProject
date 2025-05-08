package com.example.questapplication;



import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class EventsFragment extends Fragment {

    private ViewPager2 viewPagerEvents;
    private TabLayout tabLayoutEvents;

    public EventsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events, container, false);

        tabLayoutEvents = view.findViewById(R.id.eventsTabLayout);
        viewPagerEvents = view.findViewById(R.id.eventsViewPager);

        // Set up ViewPager2 adapter
        EventsPagerAdapter adapter = new EventsPagerAdapter(this);
        viewPagerEvents.setAdapter(adapter);

        // Link TabLayout with ViewPager2
        new TabLayoutMediator(tabLayoutEvents, viewPagerEvents,
                (tab, position) -> {
                    if (position == 0) tab.setText("Create or Join Events");
                    else if (position == 1) tab.setText("Manage Events");
                }).attach();

        // Optional: default to the first tab
        if (getArguments() != null && "joinedEvents".equals(getArguments().getString("navigateTo"))) {
            viewPagerEvents.setCurrentItem(1, false); // Switch to Manage Events tab
        }


        return view;
    }
}
