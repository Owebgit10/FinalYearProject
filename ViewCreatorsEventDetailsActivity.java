package com.example.questapplication;


import androidx.annotation.NonNull;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.database.*;

public class ViewCreatorsEventDetailsActivity extends AppCompatActivity {

    private static final String TAG = "ViewCreatorsEventDetails";

    private ImageView eventImage;
    private TextView eventName, eventLocation, eventSpaces, eventDate, eventTime, eventDescription;
    private Button applyButton;

    private Event event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_creators_event_details);

        MaterialToolbar toolbar = findViewById(R.id.toolbarViewCreatorsEventDetails);
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        eventImage = findViewById(R.id.eventImage);
        eventName = findViewById(R.id.eventName);
        eventLocation = findViewById(R.id.eventLocation);
        eventSpaces = findViewById(R.id.eventSpaces);
        eventDate = findViewById(R.id.eventDate);
        eventTime = findViewById(R.id.eventTime);
        eventDescription = findViewById(R.id.eventDescription);
        applyButton = findViewById(R.id.applyToEventButton);

        Event passedEvent = (Event) getIntent().getSerializableExtra("event");

        if (passedEvent != null) {
            fetchLatestEventData(passedEvent.getId());
        } else {
            Toast.makeText(this, "Failed to load event details.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void fetchLatestEventData(String eventId) {
        DatabaseReference eventRef = FirebaseDatabase.getInstance().getReference("Events").child(eventId);
        eventRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Event updatedEvent = snapshot.getValue(Event.class);
                if (updatedEvent != null) {
                    event = updatedEvent;
                    populateViews(event);
                } else {
                    Toast.makeText(ViewCreatorsEventDetailsActivity.this, "Event no longer exists.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ViewCreatorsEventDetailsActivity.this, "Error loading event", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Firebase error", error.toException());
            }
        });
    }


    private void populateViews(Event event) {
        eventName.setText(event.getName());
        eventLocation.setText(event.getLocation());
        eventDate.setText(event.getDate());
        eventTime.setText(event.getTime());
        eventDescription.setText(event.getDescription());

        String spotsLeft = getString(R.string.spots_left, event.getAvailableSpaces());
        eventSpaces.setText(spotsLeft);

        if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
            Glide.with(this).load(event.getImageUrl()).into(eventImage);
            eventImage.setVisibility(View.VISIBLE);
        } else {
            eventImage.setVisibility(View.GONE);
        }

        applyButton.setOnClickListener(v -> {
            Intent intent = new Intent(ViewCreatorsEventDetailsActivity.this, ApplyToEventActivity.class);
            intent.putExtra("event", event); // passing the latest version of the event
            startActivity(intent);
        });
    }
}





