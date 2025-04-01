package com.example.questapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditProfileActivity extends AppCompatActivity {

    private TextView usernameDisplay, emailDisplay;
    private EditText firstNameInput, lastNameInput, phoneInput;
    private Button saveProfileButton;

    private DatabaseReference userRef;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile_activity);

        usernameDisplay = findViewById(R.id.usernameDisplay);
        emailDisplay = findViewById(R.id.emailDisplay);
        firstNameInput = findViewById(R.id.firstNameInput);
        lastNameInput = findViewById(R.id.lastNameInput);
        phoneInput = findViewById(R.id.phoneInput);
        saveProfileButton = findViewById(R.id.saveProfileButton);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            uid = currentUser.getUid();
            userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);

            // Use string resources with placeholders
            String formattedUsername = getString(R.string.display_username, currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "N/A");
            usernameDisplay.setText(formattedUsername);

            String formattedEmail = getString(R.string.display_email, currentUser.getEmail() != null ? currentUser.getEmail() : getString(R.string.no_email_found));
            emailDisplay.setText(formattedEmail);
        }

        // 🔓 Make inputs editable when tapped
        enableOnClick(firstNameInput);
        enableOnClick(lastNameInput);
        enableOnClick(phoneInput);

        // ✅ Save button
        saveProfileButton.setOnClickListener(v -> {
            String first = firstNameInput.getText().toString().trim();
            String last = lastNameInput.getText().toString().trim();
            String phone = phoneInput.getText().toString().trim();

            if (first.isEmpty() || last.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            userRef.child("firstName").setValue(first);
            userRef.child("lastName").setValue(last);
            userRef.child("phone").setValue(phone);

            Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
        });
    }

    // Utility to make field editable when tapped
    private void enableOnClick(EditText input) {
        input.setOnClickListener(v -> {
            input.setEnabled(true);
            input.requestFocus();
        });
    }
}

