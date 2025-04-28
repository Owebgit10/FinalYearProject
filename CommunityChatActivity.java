package com.example.questapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.*;

public class CommunityChatActivity extends AppCompatActivity {

    private EditText editMessageInput;
    private ImageButton btnSendMessage, btnSendImage, btnRemoveImage;
    private ImageView imagePreview;
    private LinearLayout imagePreviewContainer;
    private RecyclerView recyclerViewMessages;
    private ChatMessagesAdapter messagesAdapter;

    private final List<ChatMessage> messageList = new ArrayList<>();

    private String communityId;
    private String currentUserId;
    private String currentUserName;

    private static final int PICK_IMAGE_REQUEST = 1001;
    private Uri selectedImageUri;

    private Community community;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_chat);

        // Get Community object
        community = (Community) getIntent().getSerializableExtra("community");
        if (community == null) {
            Toast.makeText(this, "Missing community data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        communityId = community.getId();
        currentUserId = FirebaseAuth.getInstance().getUid();

        // Toolbar setup
        MaterialToolbar toolbar = findViewById(R.id.chatToolbar);
        toolbar.setTitle(community.getName() + " Community Chat");
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        editMessageInput = findViewById(R.id.editMessageInput);
        btnSendMessage = findViewById(R.id.btnSendMessage);
        btnSendImage = findViewById(R.id.btnSendImage);
        btnRemoveImage = findViewById(R.id.btnRemoveImage);
        imagePreview = findViewById(R.id.imagePreview);
        imagePreviewContainer = findViewById(R.id.imagePreviewContainer);
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);

        // Recycler setup
        messagesAdapter = new ChatMessagesAdapter(this, messageList, currentUserId);
        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMessages.setAdapter(messagesAdapter);

        loadCurrentUserName();
        loadMessages();

        btnSendMessage.setOnClickListener(v -> sendMessage());
        btnSendImage.setOnClickListener(v -> openImagePicker());
        btnRemoveImage.setOnClickListener(v -> clearImagePreview());
    }

    private void loadCurrentUserName() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentUserName = snapshot.child("firstName").getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CommunityChatActivity.this, "Failed to load user info", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMessages() {
        DatabaseReference messagesRef = FirebaseDatabase.getInstance()
                .getReference("CommunityChats")
                .child(communityId)
                .child("Messages");

        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot msgSnap : snapshot.getChildren()) {
                    ChatMessage message = msgSnap.getValue(ChatMessage.class);
                    if (message != null) {
                        messageList.add(message);
                    }
                }
                messagesAdapter.notifyDataSetChanged();
                recyclerViewMessages.scrollToPosition(messageList.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CommunityChatActivity.this, "Failed to load messages", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage() {
        String text = editMessageInput.getText().toString().trim();
        boolean hasText = !TextUtils.isEmpty(text);
        boolean hasImage = selectedImageUri != null;

        if (!hasText && !hasImage) return;

        String messageId = UUID.randomUUID().toString();

        if (hasImage) {
            uploadImageThenSend(messageId, text);
        } else {
            ChatMessage message = new ChatMessage(
                    messageId, currentUserId, currentUserName,
                    text, null, System.currentTimeMillis());

            sendMessageToDatabase(message);
        }
    }

    private void uploadImageThenSend(String messageId, String optionalText) {
        String fileName = UUID.randomUUID().toString();
        StorageReference imageRef = FirebaseStorage.getInstance()
                .getReference("ChatImages")
                .child(communityId)
                .child(fileName);

        imageRef.putFile(selectedImageUri).addOnSuccessListener(taskSnapshot ->
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    ChatMessage message = new ChatMessage(
                            messageId,
                            currentUserId,
                            currentUserName,
                            optionalText.isEmpty() ? null : optionalText,
                            uri.toString(),
                            System.currentTimeMillis());

                    sendMessageToDatabase(message);
                    clearImagePreview();
                })
        ).addOnFailureListener(e ->
                Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show());
    }

    private void sendMessageToDatabase(ChatMessage message) {
        FirebaseDatabase.getInstance().getReference("CommunityChats")
                .child(communityId)
                .child("Messages")
                .child(message.getMessageId())
                .setValue(message)
                .addOnSuccessListener(aVoid -> editMessageInput.setText(""))
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            Glide.with(this).load(selectedImageUri).into(imagePreview);
            imagePreviewContainer.setVisibility(View.VISIBLE);
        }
    }

    private void clearImagePreview() {
        selectedImageUri = null;
        imagePreview.setImageDrawable(null);
        imagePreviewContainer.setVisibility(View.GONE);
    }
}




