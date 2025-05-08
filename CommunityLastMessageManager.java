package com.example.questapplication;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Map;

public class CommunityLastMessageManager {

    private final Map<String, ValueEventListener> listeners = new HashMap<>();

    public interface OnLastMessageFetchedListener {
        void onLastMessageFetched(String communityId, String lastMessage);
    }

    public void listenForLastMessage(String communityId, OnLastMessageFetchedListener listener) {
        DatabaseReference messagesRef = FirebaseDatabase.getInstance()
                .getReference("CommunityChats")
                .child(communityId)
                .child("Messages");

        ValueEventListener messageListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String latestMessage = null;
                long latestTimestamp = 0;

                for (DataSnapshot messageSnap : snapshot.getChildren()) {
                    ChatMessage chatMessage = messageSnap.getValue(ChatMessage.class);
                    if (chatMessage != null && chatMessage.getTimestamp() > latestTimestamp) {
                        latestTimestamp = chatMessage.getTimestamp();
                        if (chatMessage.getText() != null && !chatMessage.getText().isEmpty()) {
                            latestMessage = chatMessage.getText();
                        } else if (chatMessage.getImageUrl() != null) {
                            latestMessage = "📷 Image";
                        }
                    }
                }

                listener.onLastMessageFetched(communityId, latestMessage != null ? latestMessage : "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Optionally log or handle the error
            }
        };

        messagesRef.addValueEventListener(messageListener);
        listeners.put(communityId, messageListener);
    }

    public void removeListener(String communityId) {
        DatabaseReference messagesRef = FirebaseDatabase.getInstance()
                .getReference("CommunityChats")
                .child(communityId)
                .child("Messages");

        ValueEventListener listener = listeners.get(communityId);
        if (listener != null) {
            messagesRef.removeEventListener(listener);
            listeners.remove(communityId);
        }
    }

    public void clearAllListeners() {
        for (Map.Entry<String, ValueEventListener> entry : listeners.entrySet()) {
            removeListener(entry.getKey());
        }
    }
}

