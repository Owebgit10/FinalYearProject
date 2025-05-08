package com.example.questapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.HashMap;
import java.util.Map;

public class ApplyToEventActivity extends AppCompatActivity {

    private EditText firstNameInput, lastNameInput, phoneInput, emailInput, messageInput, spacesInput;
    private Button applyNowButton;

    private FirebaseUser currentUser;
    private DatabaseReference usersRef;
    private Event event;
    private int previousSpacesRequested = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply_to_event);

        MaterialToolbar toolbar = findViewById(R.id.toolbarApplyToEvent);
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        firstNameInput = findViewById(R.id.firstNameInput);
        lastNameInput = findViewById(R.id.lastNameInput);
        phoneInput = findViewById(R.id.phoneInput);
        emailInput = findViewById(R.id.emailInput);
        messageInput = findViewById(R.id.messageInput);
        spacesInput = findViewById(R.id.spacesInput);
        applyNowButton = findViewById(R.id.applyNowButton);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        event = (Event) getIntent().getSerializableExtra("event");

        if (currentUser != null && event != null) {
            fetchLatestEventData(event.getId());
            usersRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());

            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    firstNameInput.setText(snapshot.child("firstName").getValue(String.class));
                    lastNameInput.setText(snapshot.child("lastName").getValue(String.class));
                    phoneInput.setText(snapshot.child("phone").getValue(String.class));
                    emailInput.setText(currentUser.getEmail());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ApplyToEventActivity.this, "Failed to load user info", Toast.LENGTH_SHORT).show();
                }
            });
        }

        applyNowButton.setOnClickListener(v -> submitApplication());
    }

    private void fetchLatestEventData(String eventId) {
        DatabaseReference eventRef = FirebaseDatabase.getInstance().getReference("Events").child(eventId);
        eventRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Event updatedEvent = snapshot.getValue(Event.class);
                if (updatedEvent != null) {
                    event = updatedEvent;

                    // Update hint for spaces input
                    String hintText = "Spaces you would like to book (Max: " + event.getAvailableSpaces() + ")";
                    spacesInput.setHint(hintText);

                    // Fetch previous application if it exists
                    String userId = currentUser.getUid();
                    FirebaseDatabase.getInstance().getReference("EventApplications")
                            .child(event.getId())
                            .child(userId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        Integer prev = snapshot.child("spacesRequested").getValue(Integer.class);
                                        previousSpacesRequested = (prev != null) ? prev : 0;
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ApplyToEventActivity.this, "Failed to load event info", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitApplication() {
        String first = firstNameInput.getText().toString().trim();
        String last = lastNameInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String message = messageInput.getText().toString().trim();
        String spacesStr = spacesInput.getText().toString().trim();

        if (first.isEmpty() || last.isEmpty() || phone.isEmpty() || email.isEmpty() || spacesStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int spacesRequested;
        try {
            spacesRequested = Integer.parseInt(spacesStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Spaces must be a number", Toast.LENGTH_SHORT).show();
            return;
        }

        int difference = spacesRequested - previousSpacesRequested;
        if (difference > event.getAvailableSpaces()) {
            Toast.makeText(this, "Only " + event.getAvailableSpaces() + " spots left.", Toast.LENGTH_LONG).show();
            return;
        }

        String userId = currentUser.getUid();
        String eventId = event.getId();
        int newAvailableSpaces = event.getAvailableSpaces() - difference;

        // Update availableSpaces
        FirebaseDatabase.getInstance().getReference("Events")
                .child(eventId)
                .child("availableSpaces")
                .setValue(newAvailableSpaces);

        // Application data
        Map<String, Object> appData = new HashMap<>();
        appData.put("firstName", first);
        appData.put("lastName", last);
        appData.put("phone", phone);
        appData.put("email", email);
        appData.put("message", message);
        appData.put("spacesRequested", spacesRequested);
        appData.put("timestamp", System.currentTimeMillis());

        DatabaseReference appRef = FirebaseDatabase.getInstance()
                .getReference("EventApplications")
                .child(eventId)
                .child(userId);

        appRef.setValue(appData).addOnSuccessListener(unused -> {
            FirebaseDatabase.getInstance()
                    .getReference("EventParticipants")
                    .child(eventId)
                    .child(userId)
                    .setValue(true);

            Toast.makeText(this, "Application submitted!", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("navigateTo", "joinedEvents");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to submit application", Toast.LENGTH_SHORT).show();
        });
    }
}





