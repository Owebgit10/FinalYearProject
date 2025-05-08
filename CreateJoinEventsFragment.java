package com.example.questapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.*;

public class CreateJoinEventsFragment extends Fragment {

    private RecyclerView eventList;
    private CreateJoinEventsAdapter adapter;
    private final List<Event> allEvents = new ArrayList<>();
    private final List<Event> filteredList = new ArrayList<>();

    private DatabaseReference eventsRef;
    private String currentUserId;

    private ClearableEditText searchInput;
    private Button createEventBtn, searchBtn;
    private TextView autocompleteSuggestion, noEventsText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_or_join_events, container, false);

        createEventBtn = view.findViewById(R.id.btnNavigateToCreateEvent);
        searchInput = view.findViewById(R.id.searchInput);
        searchBtn = view.findViewById(R.id.btnSearchCommunities); // reuse this button ID
        autocompleteSuggestion = view.findViewById(R.id.autocompleteSuggestion);
        eventList = view.findViewById(R.id.eventList);
        noEventsText = view.findViewById(R.id.noEventsText);

        eventList.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CreateJoinEventsAdapter(getContext(), filteredList);
        eventList.setAdapter(adapter);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();
        } else {
            // Handle the case where user is not signed in (optional: redirect or show message)
            currentUserId = null;
        }

        eventsRef = FirebaseDatabase.getInstance().getReference("Events");

        createEventBtn.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), EventCreationActivity.class));
        });

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().toLowerCase(Locale.ROOT).trim();
                showAutocompleteSuggestion(query);
                filterEventsLive(query);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        searchBtn.setOnClickListener(v -> {
            Editable editable = searchInput.getText();
            String query = editable != null ? editable.toString().toLowerCase(Locale.ROOT).trim() : "";
            filterEventsWithRelevance(query);
        });

        return view;
    }

    private void loadOtherUsersEvents() {
        eventsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allEvents.clear();
                for (DataSnapshot eventSnap : snapshot.getChildren()) {
                    Event event = eventSnap.getValue(Event.class);
                    if (event != null && !event.getUserId().equals(currentUserId)) {
                        allEvents.add(event);
                    }
                }
                filterEventsLive(searchInput.getText() != null ? searchInput.getText().toString() : "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void filterEventsLive(String query) {
        filteredList.clear();
        String search = query.replaceAll("[^a-zA-Z0-9]", "").toLowerCase(Locale.ROOT);

        for (Event event : allEvents) {
            boolean nameMatch = event.getNameSearch() != null && event.getNameSearch().contains(search);
            boolean locationMatch = event.getLocationSearch() != null && event.getLocationSearch().contains(search);
            boolean categoryMatch = event.getCategorySearch() != null && event.getCategorySearch().contains(search);
            boolean tagMatch = false;

            List<String> tags = event.getTagsSearch() != null ? event.getTagsSearch() : new ArrayList<>();
            for (String tag : tags) {
                if (tag.contains(search)) {
                    tagMatch = true;
                    break;
                }
            }

            if (nameMatch || locationMatch || categoryMatch || tagMatch) {
                filteredList.add(event);
            }
        }

        adapter.updateList(filteredList);
        noEventsText.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void filterEventsWithRelevance(String query) {
        filteredList.clear();
        String search = query.replaceAll("[^a-zA-Z0-9]", "").toLowerCase(Locale.ROOT);

        for (Event event : allEvents) {
            int score = calculateRelevanceScore(event, search);
            if (score > 0) {
                event.setRelevanceScore(score);
                filteredList.add(event);
            }
        }

        Collections.sort(filteredList, (a, b) -> Integer.compare(b.getRelevanceScore(), a.getRelevanceScore()));
        adapter.updateList(filteredList);
        noEventsText.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private int calculateRelevanceScore(Event event, String search) {
        int score = 0;

        String name = event.getNameSearch() != null ? event.getNameSearch() : "";
        String location = event.getLocationSearch() != null ? event.getLocationSearch() : "";
        String category = event.getCategorySearch() != null ? event.getCategorySearch() : "";
        List<String> tags = event.getTagsSearch() != null ? event.getTagsSearch() : new ArrayList<>();

        if (name.contains(search)) score += 5;
        if (location.contains(search)) score += 4;
        if (category.contains(search)) score += 3;

        for (String tag : tags) {
            if (tag.contains(search)) {
                score += 4;
                if (name.contains(tag)) score += 2;
            }
        }

        return score;
    }

    private void showAutocompleteSuggestion(String input) {
        if (TextUtils.isEmpty(input)) {
            autocompleteSuggestion.setVisibility(View.GONE);
            return;
        }

        for (Event event : allEvents) {
            if (event.getNameSearch() != null && event.getNameSearch().startsWith(input)) {
                autocompleteSuggestion.setText(event.getName());
                autocompleteSuggestion.setVisibility(View.VISIBLE);
                return;
            }
            if (event.getTagsSearch() != null) {
                for (String tag : event.getTagsSearch()) {
                    if (tag.startsWith(input)) {
                        autocompleteSuggestion.setText(tag);
                        autocompleteSuggestion.setVisibility(View.VISIBLE);
                        return;
                    }
                }
            }
        }

        autocompleteSuggestion.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadOtherUsersEvents();
    }
}





