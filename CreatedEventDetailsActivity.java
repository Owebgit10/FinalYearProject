package com.example.questapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import android.util.Log;

public class CreatedEventDetailsActivity extends AppCompatActivity {

    private ImageView eventImage;
    private TextView eventName, eventLocation, eventDate, eventTime, eventDescription;
    private Button updateEventButtonEvent, cancelEventButtonEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_created_event_details);

        Toast.makeText(this, "Event Details Screen Opened", Toast.LENGTH_SHORT).show();

        // Toolbar setup
        MaterialToolbar createdEventDetailsToolBar = findViewById(R.id.toolbarCreatedEventDetails);
        createdEventDetailsToolBar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        // UI references
        eventImage = findViewById(R.id.eventImage);
        eventName = findViewById(R.id.eventName);
        eventLocation = findViewById(R.id.eventLocation);
        eventDate = findViewById(R.id.eventDate);
        eventTime = findViewById(R.id.eventTime);
        eventDescription = findViewById(R.id.eventDescription);
        updateEventButtonEvent = findViewById(R.id.updateEventButton);
        cancelEventButtonEvent = findViewById(R.id.cancelEventButton);

        // Get event
        Event event = (Event) getIntent().getSerializableExtra("event");

        if (event != null) {
            eventName.setText(event.getName());
            eventLocation.setText(event.getLocation());
            eventDate.setText(event.getDate());
            eventTime.setText(event.getTime());
            eventDescription.setText(event.getDescription());

            if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
                Glide.with(this).load(event.getImageUrl()).into(eventImage);
                eventImage.setVisibility(View.VISIBLE);
            } else {
                eventImage.setVisibility(View.GONE);
            }
        }

        updateEventButtonEvent.setOnClickListener(v -> {
            if (event != null) {
                Intent intent = new Intent(CreatedEventDetailsActivity.this, UpdateEventActivity.class);
                intent.putExtra("event", event);
                startActivity(intent);
            } else {
                Toast.makeText(this, "No event data found to update.", Toast.LENGTH_SHORT).show();
            }
        });

        cancelEventButtonEvent.setOnClickListener(v -> {
            if (event == null) {
                Toast.makeText(this, "Event data not found.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Confirmation dialog
            new AlertDialog.Builder(this)
                    .setTitle("Cancel Event")
                    .setMessage("Are you sure you want to cancel this event?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Proceed with deletion
                        DatabaseReference eventRef = FirebaseDatabase.getInstance().getReference("Events").child(event.getId());
                        DatabaseReference participantsRef = FirebaseDatabase.getInstance().getReference("EventParticipants").child(event.getId());

                        eventRef.removeValue()
                                .addOnSuccessListener(unused -> {
                                    participantsRef.removeValue();

                                    // Delete image if it exists
                                    if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
                                        StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(event.getImageUrl());
                                        imageRef.delete()
                                                .addOnSuccessListener(aVoid -> Log.d("CancelEvent", "Image deleted"))
                                                .addOnFailureListener(e -> Log.w("CancelEvent", "Image delete failed", e));
                                    }

                                    Toast.makeText(this, "Event cancelled successfully.", Toast.LENGTH_SHORT).show();
                                    finish(); // Return to previous screen
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to cancel event.", Toast.LENGTH_SHORT).show();
                                    Log.e("CancelEvent", "Database deletion failed", e);
                                });
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        dialog.dismiss();
                        Toast.makeText(this, "Event not cancelled.", Toast.LENGTH_SHORT).show();
                    })
                    .show();
        });
    }
}




