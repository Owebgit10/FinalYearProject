package com.example.questapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.List;

public class ViewCommunityDetails extends AppCompatActivity {

    private ImageView imageCommunityBanner;
    private TextView textCommunityName, textCommunityDescription, rulesHeader;
    private LinearLayout rulesContainer;
    private Button btnJoinOrChat;

    private Community community;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_community_details);

        MaterialToolbar toolbar = findViewById(R.id.communityDetailsToolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        imageCommunityBanner = findViewById(R.id.imageCommunityBanner);
        textCommunityName = findViewById(R.id.textCommunityName);
        textCommunityDescription = findViewById(R.id.textCommunityDescription);
        rulesContainer = findViewById(R.id.rulesContainer);
        rulesHeader = findViewById(R.id.rulesHeader);
        btnJoinOrChat = findViewById(R.id.btnJoinOrChat);

        community = (Community) getIntent().getSerializableExtra("community");
        currentUserId = FirebaseAuth.getInstance().getUid();

        if (community == null) {
            Toast.makeText(this, getString(R.string.missing_community_data), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        displayCommunityDetails();
        checkMembershipStatus();
    }

    private void displayCommunityDetails() {
        textCommunityName.setText(community.getName());
        textCommunityDescription.setText(community.getDescription());

        if (community.getImageUrl() != null && !community.getImageUrl().isEmpty()) {
            Glide.with(this).load(community.getImageUrl()).into(imageCommunityBanner);
        }

        List<String> rules = community.getRules();
        if (rules != null && !rules.isEmpty()) {
            rulesHeader.setVisibility(TextView.VISIBLE);
            for (String rule : rules) {
                TextView ruleView = new TextView(this);
                ruleView.setText(getString(R.string.bullet_rule, rule));
                ruleView.setTextColor(ContextCompat.getColor(this, android.R.color.white));
                ruleView.setTextSize(15);
                ruleView.setPadding(0, 4, 0, 4);
                rulesContainer.addView(ruleView);
            }
        }
    }

    private void checkMembershipStatus() {
        DatabaseReference memberRef = FirebaseDatabase.getInstance()
                .getReference("CommunityMembers")
                .child(community.getId())
                .child(currentUserId);

        memberRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    btnJoinOrChat.setText(getString(R.string.view_chat));
                    btnJoinOrChat.setOnClickListener(v -> openChat());
                } else {
                    btnJoinOrChat.setText(getString(R.string.join_community));
                    btnJoinOrChat.setOnClickListener(v -> joinCommunity());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ViewCommunityDetails.this, getString(R.string.check_membership_failed), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void joinCommunity() {
        FirebaseDatabase.getInstance()
                .getReference("CommunityMembers")
                .child(community.getId())
                .child(currentUserId)
                .setValue(true)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, getString(R.string.joined_community_toast), Toast.LENGTH_SHORT).show();
                    btnJoinOrChat.setText(getString(R.string.view_chat));
                    btnJoinOrChat.setOnClickListener(v -> openChat()); // 🔁 Setup after joining
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, getString(R.string.join_failed_toast), Toast.LENGTH_SHORT).show();
                });
    }

    private void openChat() {
        Toast.makeText(this, getString(R.string.opening_chat_toast), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, CommunityChatActivity.class);
        intent.putExtra("community", community); // 🧠 Pass community object
        startActivity(intent);
    }
}



