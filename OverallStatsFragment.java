package com.example.questapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.*;

public class OverallStatsFragment extends Fragment {

    private RecyclerView recommendedCommunitiesRecycler;
    private TextView overallStatsPlaceholder;
    private JoinCommunitiesAdapter adapter;
    private final List<Community> recommendedList = new ArrayList<>();

    private DatabaseReference db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_overall_stats, container, false);

        recommendedCommunitiesRecycler = view.findViewById(R.id.recommendedCommunitiesRecycler);
        overallStatsPlaceholder = view.findViewById(R.id.overallStatsPlaceholder);

        recommendedCommunitiesRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new JoinCommunitiesAdapter(getContext(), recommendedList);
        recommendedCommunitiesRecycler.setAdapter(adapter);

        db = FirebaseDatabase.getInstance().getReference();
        fetchGoalsAndRecommendCommunities();

        return view;
    }

    private void fetchGoalsAndRecommendCommunities() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String userId = user.getUid();
        db.child("Goals").child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<String> keywords = new ArrayList<>();

                        for (DataSnapshot goalSnap : snapshot.getChildren()) {
                            Goal goal = goalSnap.getValue(Goal.class);
                            if (goal != null) {
                                if (goal.getTitleSearch() != null)
                                    keywords.add(goal.getTitleSearch());

                                if (goal.getTasks() != null) {
                                    for (Task task : goal.getTasks().values()) {
                                        if (task.getTitleSearch() != null) {
                                            keywords.add(task.getTitleSearch());
                                        }
                                    }
                                }
                            }
                        }

                        fetchAndScoreCommunities(keywords);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void fetchAndScoreCommunities(List<String> keywords) {
        db.child("Communities").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                recommendedList.clear();

                for (DataSnapshot commSnap : snapshot.getChildren()) {
                    Community community = commSnap.getValue(Community.class);
                    if (community == null) continue;

                    int score = 0;

                    for (String keyword : keywords) {
                        if (keyword == null) continue;

                        // +5 for exact title match
                        if (keyword.equals(community.getNameSearch())) {
                            score += 5;
                        }

                        // +3 if in tagsSearch
                        if (community.getTagsSearch() != null && community.getTagsSearch().contains(keyword)) {
                            score += 3;
                        }

                        // +1 if partial match in name or tags
                        if (community.getNameSearch() != null && community.getNameSearch().contains(keyword)) {
                            score += 1;
                        }

                        if (community.getTagsSearch() != null) {
                            for (String tag : community.getTagsSearch()) {
                                if (tag.contains(keyword)) {
                                    score += 1;
                                }
                            }
                        }
                    }

                    if (score > 0) {
                        community.setRelevanceScore(score);
                        recommendedList.add(community);
                    }
                }

                // Sort by highest score first
                Collections.sort(recommendedList, new Comparator<Community>() {
                    @Override
                    public int compare(Community a, Community b) {
                        return Integer.compare(b.getRelevanceScore(), a.getRelevanceScore());
                    }
                });

                adapter.notifyDataSetChanged();
                updatePlaceholder();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void updatePlaceholder() {
        if (recommendedList.isEmpty()) {
            overallStatsPlaceholder.setText(getString(R.string.no_recommended_communities));

            overallStatsPlaceholder.setVisibility(View.VISIBLE);
            recommendedCommunitiesRecycler.setVisibility(View.GONE);
        } else {
            overallStatsPlaceholder.setVisibility(View.GONE);
            recommendedCommunitiesRecycler.setVisibility(View.VISIBLE);
        }
    }
}



