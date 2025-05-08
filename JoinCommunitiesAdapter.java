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
import com.google.firebase.database.*;

import java.util.List;

public class JoinCommunitiesAdapter extends RecyclerView.Adapter<JoinCommunitiesAdapter.ViewHolder> {

    private final Context context;
    private final List<Community> communityList;

    public JoinCommunitiesAdapter(Context context, List<Community> communityList) {
        this.context = context;
        this.communityList = communityList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_join_community_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Community community = communityList.get(position);

        holder.communityName.setText(community.getName());

        // Load image if available
        if (community.getImageUrl() != null && !community.getImageUrl().isEmpty()) {
            Glide.with(context).load(community.getImageUrl()).into(holder.communityImage);
        } else {
            holder.communityImage.setImageResource(R.drawable.ic_upload_placeholder);
        }

        // Fetch number of members
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("CommunityMembers")
                .child(community.getId());

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int memberCount = (int) snapshot.getChildrenCount();
                String memberText = context.getResources().getQuantityString(
                        R.plurals.members_joined,
                        memberCount,
                        memberCount
                );
                holder.membersJoined.setText(memberText);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                holder.membersJoined.setText(context.getString(R.string.members_joined_default));
            }
        });

        // 👉 Handle card click
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ViewCommunityDetails.class);
            intent.putExtra("community", community);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return communityList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView communityImage;
        TextView communityName, membersJoined, joinNowLabel;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            communityImage = itemView.findViewById(R.id.communityImage);
            communityName = itemView.findViewById(R.id.communityName);
            membersJoined = itemView.findViewById(R.id.membersJoined);
            joinNowLabel = itemView.findViewById(R.id.joinNowLabel);
        }
    }
}




