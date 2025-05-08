package com.example.questapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class MyCommunitiesFragment extends Fragment {

    private RecyclerView recyclerView;
    private MyCommunityAdapter adapter;
    private final List<Community> myCommunities = new ArrayList<>();
    private final List<String> lastMessages = new ArrayList<>();

    private DatabaseReference communitiesRef, communityMembersRef;
    private FirebaseUser currentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_communities, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewMyCommunities);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MyCommunityAdapter(requireContext(), myCommunities, lastMessages);
        recyclerView.setAdapter(adapter);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        communitiesRef = FirebaseDatabase.getInstance().getReference("Communities");
        communityMembersRef = FirebaseDatabase.getInstance().getReference("CommunityMembers");

        loadMyCommunities();

        return view;
    }

    private void loadMyCommunities() {
        if (currentUser == null) return;

        String userId = currentUser.getUid();

        communityMembersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot memberSnapshot) {
                List<String> joinedCommunityIds = new ArrayList<>();

                for (DataSnapshot communitySnap : memberSnapshot.getChildren()) {
                    if (communitySnap.hasChild(userId)) {
                        joinedCommunityIds.add(communitySnap.getKey());
                    }
                }

                communitiesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        myCommunities.clear();
                        lastMessages.clear();

                        for (DataSnapshot snap : snapshot.getChildren()) {
                            Community community = snap.getValue(Community.class);
                            if (community == null) continue;

                            boolean isCreator = userId.equals(community.getUserId());
                            boolean isMember = joinedCommunityIds.contains(community.getId());

                            if (isCreator || isMember) {
                                myCommunities.add(community);
                                lastMessages.add(""); // placeholder
                            }
                        }

                        adapter.notifyDataSetChanged();
                        loadLastMessages();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        String msg = "Communities fetch failed: " + error.getMessage();
                        Log.e("MyCommunitiesFragment", msg);
                        Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                String msg = "CommunityMembers fetch failed: " + error.getMessage();
                Log.e("MyCommunitiesFragment", msg);
                Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadLastMessages() {
        for (int i = 0; i < myCommunities.size(); i++) {
            lastMessages.set(i, ""); // ensure placeholders exist
        }

        int total = myCommunities.size();
        int[] loadedCount = {0};

        for (int index = 0; index < total; index++) {
            Community community = myCommunities.get(index);
            final int currentIndex = index;

            DatabaseReference messagesRef = FirebaseDatabase.getInstance()
                    .getReference("CommunityChats")
                    .child(community.getId())
                    .child("Messages");

            messagesRef.orderByChild("timestamp").limitToLast(1)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String lastMessageText = "";

                            for (DataSnapshot messageSnap : snapshot.getChildren()) {
                                ChatMessage message = messageSnap.getValue(ChatMessage.class);
                                if (message != null) {
                                    lastMessageText = message.getText() != null ? message.getText() : "[Image]";
                                }
                            }

                            lastMessages.set(currentIndex, lastMessageText);
                            loadedCount[0]++;
                            if (loadedCount[0] == total) {
                                adapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            String msg = "Last message fetch failed: " + error.getMessage();
                            Log.e("MyCommunitiesFragment", msg);
                            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();

                            lastMessages.set(currentIndex, "");
                            loadedCount[0]++;
                            if (loadedCount[0] == total) {
                                adapter.notifyDataSetChanged();
                            }
                        }
                    });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadMyCommunities(); // refresh
    }
}




