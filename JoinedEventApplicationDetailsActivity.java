package com.example.questapplication;


import android.content.Intent;
import android.net.Uri;

import java.util.Locale;


import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class JoinedEventApplicationDetailsActivity extends AppCompatActivity {

    private TextView eventName, eventLocation, eventDate, eventTime, spacesBooked, applicantName;
    private Button viewInMapsButton, getDirectionsButton, withdrawButton;

    private Event event;
    private int spacesBookedInt;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joined_event_application_details);

        MaterialToolbar toolbar = findViewById(R.id.toolbarJoinedEventDetails);
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());


        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        currentUserId = currentUser.getUid();

        event = (Event) getIntent().getSerializableExtra("event");
        spacesBookedInt = getIntent().getIntExtra("spacesBooked", 1);

        eventName = findViewById(R.id.eventName);
        eventLocation = findViewById(R.id.eventLocation);
        eventDate = findViewById(R.id.eventDate);
        eventTime = findViewById(R.id.eventTime);
        spacesBooked = findViewById(R.id.spacesBooked);
        applicantName = findViewById(R.id.applicantName);
        viewInMapsButton = findViewById(R.id.viewInMapsButton);
        getDirectionsButton = findViewById(R.id.getDirectionsButton);
        withdrawButton = findViewById(R.id.withdrawButton);

        populateUI();

        withdrawButton.setOnClickListener(v -> handleWithdraw());
        viewInMapsButton.setOnClickListener(v -> openInMaps());
        getDirectionsButton.setOnClickListener(v -> openDirections());

    }

    private void populateUI() {
        if (event == null) return;

        eventName.setText(event.getName());
        eventLocation.setText(event.getLocation());
        eventDate.setText(event.getDate());
        eventTime.setText(event.getTime());
        spacesBooked.setText(getString(R.string.spaces_booked_label, spacesBookedInt));

        // Fetch applicant name from their application
        DatabaseReference appRef = FirebaseDatabase.getInstance()
                .getReference("EventApplications")
                .child(event.getId())
                .child(currentUserId);

        appRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String firstName = snapshot.child("firstName").getValue(String.class);
                String lastName = snapshot.child("lastName").getValue(String.class);
                String fullName = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
                applicantName.setText(getString(R.string.applicant_name_label, fullName.trim()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                applicantName.setText(R.string.applicant_name_unknown);
            }
        });
    }

    private void handleWithdraw() {
        if (event == null || currentUserId == null) return;

        DatabaseReference eventRef = FirebaseDatabase.getInstance().getReference("Events").child(event.getId());
        DatabaseReference appRef = FirebaseDatabase.getInstance().getReference("EventApplications")
                .child(event.getId()).child(currentUserId);
        DatabaseReference participantRef = FirebaseDatabase.getInstance().getReference("EventParticipants")
                .child(event.getId()).child(currentUserId);

        int updatedSpaces = event.getAvailableSpaces() + spacesBookedInt;

        eventRef.child("availableSpaces").setValue(updatedSpaces)
                .addOnSuccessListener(aVoid -> {
                    appRef.removeValue()
                            .addOnSuccessListener(unused -> {
                                participantRef.removeValue()
                                        .addOnSuccessListener(unused2 -> {
                                            Toast.makeText(this, "You have withdrawn from this event.", Toast.LENGTH_SHORT).show();
                                            finish();
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(this, "Failed to remove participant record.", Toast.LENGTH_SHORT).show());
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to remove application.", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update available spaces.", Toast.LENGTH_SHORT).show());
    }

    private void openInMaps() {
        if (event == null) return;

        double lat = event.getLatitude();
        double lng = event.getLongitude();

        if (lat == 0.0 && lng == 0.0) {
            Toast.makeText(this, "Location coordinates not available.", Toast.LENGTH_SHORT).show();
            return;
        }

        String uri = String.format(Locale.ENGLISH, "geo:%f,%f?q=%f,%f(%s)",
                lat, lng, lat, lng, Uri.encode(event.getName()));

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "Google Maps app is not installed.", Toast.LENGTH_SHORT).show();
        }
    }

    private void openDirections() {
        if (event == null) return;

        double lat = event.getLatitude();
        double lng = event.getLongitude();

        if (lat == 0.0 && lng == 0.0) {
            Toast.makeText(this, "Location coordinates not available.", Toast.LENGTH_SHORT).show();
            return;
        }

        String uri = String.format(Locale.ENGLISH,
                "https://www.google.com/maps/dir/?api=1&destination=%f,%f", lat, lng);

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "Unable to open directions.", Toast.LENGTH_SHORT).show();
        }
    }

}




