package com.example.questapplication;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class MyCommunityAdapter extends RecyclerView.Adapter<MyCommunityAdapter.ViewHolder> {

    private final Context context;
    private final List<Community> communityList;
    private final List<String> lastMessagesList;

    public MyCommunityAdapter(Context context, List<Community> communityList, List<String> lastMessagesList) {
        this.context = context;
        this.communityList = communityList;
        this.lastMessagesList = lastMessagesList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_my_community, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Community community = communityList.get(position);
        String lastMessage = (position < lastMessagesList.size()) ? lastMessagesList.get(position) : "";

        holder.communityName.setText(community.getName());
        holder.lastMessage.setText(lastMessage.isEmpty() ? context.getString(R.string.no_messages_yet) : lastMessage);

        if (community.getImageUrl() != null && !community.getImageUrl().isEmpty()) {
            Glide.with(context).load(community.getImageUrl()).into(holder.communityImage);
        } else {
            holder.communityImage.setImageResource(R.drawable.ic_upload_placeholder);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, CommunityChatActivity.class);
            intent.putExtra("community", community);
            context.startActivity(intent); // ✅ simple, clean
        });
    }

    @Override
    public int getItemCount() {
        return communityList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView communityImage;
        TextView communityName, lastMessage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            communityImage = itemView.findViewById(R.id.myCommunityImage);
            communityName = itemView.findViewById(R.id.myCommunityName);
            lastMessage = itemView.findViewById(R.id.lastMessage);
        }
    }
}
