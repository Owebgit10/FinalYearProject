package com.example.questapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ChatMessagesAdapter extends RecyclerView.Adapter<ChatMessagesAdapter.ChatViewHolder> {

    private final Context context;
    private final List<ChatMessage> messageList;
    private final String currentUserId;

    public ChatMessagesAdapter(Context context, List<ChatMessage> messageList, String currentUserId) {
        this.context = context;
        this.messageList = messageList;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = messageList.get(position);

        boolean isSelf = message.getSenderId().equals(currentUserId);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) holder.messageContainer.getLayoutParams();
        layoutParams.gravity = isSelf ? View.FOCUS_RIGHT : View.FOCUS_LEFT;
        holder.messageContainer.setLayoutParams(layoutParams);

        if (!isSelf) {
            holder.textSenderName.setVisibility(View.VISIBLE);
            holder.textSenderName.setText(message.getSenderName());
            holder.messageBubble.setBackgroundResource(R.drawable.bg_chat_bubble);
        } else {
            holder.textSenderName.setVisibility(View.GONE);
            holder.messageBubble.setBackgroundResource(R.drawable.bg_chat_bubble_self);
        }

        if (message.getText() != null && !message.getText().isEmpty()) {
            holder.textMessage.setVisibility(View.VISIBLE);
            holder.textMessage.setText(message.getText());
        } else {
            holder.textMessage.setVisibility(View.GONE);
        }

        if (message.getImageUrl() != null && !message.getImageUrl().isEmpty()) {
            holder.imageMessage.setVisibility(View.VISIBLE);
            Glide.with(context).load(message.getImageUrl()).into(holder.imageMessage);
        } else {
            holder.imageMessage.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        LinearLayout messageContainer;
        LinearLayout messageBubble;
        TextView textMessage, textSenderName;
        ImageView imageMessage;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            messageContainer = itemView.findViewById(R.id.messageContainer);
            messageBubble = itemView.findViewById(R.id.messageBubble);
            textMessage = itemView.findViewById(R.id.textMessage);
            textSenderName = itemView.findViewById(R.id.textSenderName);
            imageMessage = itemView.findViewById(R.id.imageMessage);
        }
    }
}



