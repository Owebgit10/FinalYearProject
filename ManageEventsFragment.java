package com.example.questapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class ManageEventsFragment extends Fragment {

    private Spinner eventsTypeSpinner;
    private RecyclerView eventsRecyclerView;
    private TextView emptyMessage;
    private ManageEventsAdapter adapter;
    private final List<Event> eventList = new ArrayList<>();

    private FirebaseUser currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_events, container, false);

        // UI references
        eventsTypeSpinner = view.findViewById(R.id.eventsTypeSpinner);
        eventsRecyclerView = view.findViewById(R.id.eventsRecyclerView);
        emptyMessage = view.findViewById(R.id.emptyMessage);

        // Firebase
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // RecyclerView setup
        adapter = new ManageEventsAdapter(getContext(), eventList); // ✅ Fixed line
        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        eventsRecyclerView.setAdapter(adapter);

        // Spinner listener
        eventsTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    loadCreatedEvents();
                } else {
                    showEmptyMessage("No events joined");
                    eventList.clear();
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        return view;
    }

    private void loadCreatedEvents() {
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Events");

        ref.orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        eventList.clear();

                        for (DataSnapshot child : snapshot.getChildren()) {
                            Event event = child.getValue(Event.class);
                            if (event != null) {
                                eventList.add(event);
                            }
                        }

                        adapter.notifyDataSetChanged();

                        if (eventList.isEmpty()) {
                            showEmptyMessage("No events created");
                        } else {
                            emptyMessage.setVisibility(View.GONE); // Hide the message when we have events
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Failed to load events.", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void showEmptyMessage(String message) {
        emptyMessage.setText(message);
        emptyMessage.setVisibility(View.VISIBLE);
    }
}



