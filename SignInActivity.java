package com.example.questapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SignInActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private Button signInButton;
    private TextView goToCreateAccount;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        // Check if the user is already signed in
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            // User is signed in, go directly to MainActivity
            startActivity(new Intent(this, MainActivity.class));
            finish();  // Close the current activity to prevent the user from returning to the sign-in screen
            return;   // Ensure that the rest of the onCreate method does not execute
        }

        setContentView(R.layout.signinactivity);

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        signInButton = findViewById(R.id.signInButton);
        goToCreateAccount = findViewById(R.id.goToCreateAccount);

        signInButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Either your email or Password was not input.All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        goToCreateAccount.setOnClickListener(v -> {
            startActivity(new Intent(this, CreateAccountActivity.class));
            finish();
        });
    }
}


