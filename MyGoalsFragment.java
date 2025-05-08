package com.example.questapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MyGoalsFragment extends Fragment {

    private RecyclerView goalsRecyclerView;
    private TextView noGoalsPlaceholder;
    private GoalAdapter goalAdapter;
    private final List<Goal> goalList = new ArrayList<>();

    private FirebaseUser currentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_goals, container, false);

        goalsRecyclerView = view.findViewById(R.id.goalsRecyclerView);
        noGoalsPlaceholder = view.findViewById(R.id.noGoalsPlaceholder);

        goalsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        goalAdapter = new GoalAdapter(goalList);
        goalsRecyclerView.setAdapter(goalAdapter);

        FloatingActionButton addGoalFab = view.findViewById(R.id.addGoalFab);
        addGoalFab.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CreateGoalActivity.class);
            startActivity(intent);
        });

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "You must be signed in.", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserGoals(); // 🔥 Re-fetch goals when returning
    }

    private void loadUserGoals() {
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("Goals")
                .child(userId); // ✅ Read from /Goals/{uid}

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                goalList.clear();
                for (DataSnapshot goalSnapshot : snapshot.getChildren()) {
                    Goal goal = goalSnapshot.getValue(Goal.class);
                    if (goal != null) {
                        goalList.add(goal);
                    }
                }

                // Sort: Incomplete goals first
                Collections.sort(goalList, (g1, g2) -> {
                    boolean g1Complete = isGoalComplete(g1);
                    boolean g2Complete = isGoalComplete(g2);
                    return Boolean.compare(g1Complete, g2Complete);
                });

                goalAdapter.notifyDataSetChanged();
                updatePlaceholder();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load goals.", Toast.LENGTH_SHORT).show();
                Log.e("FirebaseError", "loadUserGoals: " + error.getMessage(), error.toException());
            }
        });
    }

    private boolean isGoalComplete(Goal goal) {
        if (goal.getTasks() == null) return false;
        for (Task task : goal.getTasks().values()) {
            if (!task.isCompleted()) {
                return false;
            }
        }
        return true;
    }

    private void updatePlaceholder() {
        if (goalList.isEmpty()) {
            noGoalsPlaceholder.setVisibility(View.VISIBLE);
            goalsRecyclerView.setVisibility(View.GONE);
        } else {
            noGoalsPlaceholder.setVisibility(View.GONE);
            goalsRecyclerView.setVisibility(View.VISIBLE);
        }
    }
}







