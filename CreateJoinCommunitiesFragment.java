package com.example.questapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class CreateJoinCommunitiesFragment extends Fragment {

    private Button navigateCreateCommunityButton;
    private ClearableEditText communitySearchInput;
    private RecyclerView communityList;
    private JoinCommunitiesAdapter adapter;
    private final List<Community> communityData = new ArrayList<>();
    private final List<Community> filteredList = new ArrayList<>();
    private DatabaseReference communitiesRef;
    private FirebaseUser currentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_or_join_communities, container, false);

        // UI elements
        navigateCreateCommunityButton = view.findViewById(R.id.btnNavigateToCreateCommunity);
        communitySearchInput = view.findViewById(R.id.CommunitySearchInput);
        communityList = view.findViewById(R.id.communityList);

        // Firebase setup
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        communitiesRef = FirebaseDatabase.getInstance().getReference("Communities");

        // RecyclerView setup
        adapter = new JoinCommunitiesAdapter(requireContext(), filteredList);
        communityList.setLayoutManager(new LinearLayoutManager(getContext()));
        communityList.setAdapter(adapter);

        // Button to navigate to create screen
        navigateCreateCommunityButton.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), CommunityCreationActivity.class));
        });

        // Live search logic
        communitySearchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCommunities(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        loadCommunities();
        return view;
    }

    private void loadCommunities() {
        if (currentUser == null) return;
        String currentUserId = currentUser.getUid();

        communitiesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                communityData.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Community community = child.getValue(Community.class);
                    if (community != null && !community.getUserId().equals(currentUserId)) {
                        communityData.add(community);
                    }
                }

                // ✅ Sort by createdAt descending (newest first)
                Collections.sort(communityData, new Comparator<Community>() {
                    @Override
                    public int compare(Community c1, Community c2) {
                        return Long.compare(c2.getCreatedAt(), c1.getCreatedAt());
                    }
                });

                filterCommunities(communitySearchInput.getText().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Optional: handle error
            }
        });
    }

    private void filterCommunities(String query) {
        filteredList.clear();
        String search = query.toLowerCase(Locale.ROOT);
        for (Community community : communityData) {
            if (community.getName().toLowerCase(Locale.ROOT).contains(search) ||
                    community.getTags().toString().toLowerCase(Locale.ROOT).contains(search)) {
                filteredList.add(community);
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCommunities();
    }
}








