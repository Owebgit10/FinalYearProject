package com.example.questapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class QuestGoalPilotFragment extends Fragment {

    private ViewPager2 viewPagerGoals;
    private TabLayout tabLayoutGoals;

    public QuestGoalPilotFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quest_goal_pilot, container, false);

        tabLayoutGoals = view.findViewById(R.id.goalsTabLayout);
        viewPagerGoals = view.findViewById(R.id.goalsViewPager);

        // Set up ViewPager2 adapter
        GoalsPagerAdapter adapter = new GoalsPagerAdapter(this);
        viewPagerGoals.setAdapter(adapter);

        // Link TabLayout with ViewPager2
        new TabLayoutMediator(tabLayoutGoals, viewPagerGoals,
                (tab, position) -> {
                    if (position == 0) tab.setText("My Goals");
                    else if (position == 1) tab.setText("Recommended Communities for goals");
                }).attach();

        // Optional: default to the first tab
        viewPagerGoals.setCurrentItem(0, false);

        return view;
    }
}


