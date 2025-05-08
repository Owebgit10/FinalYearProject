package com.example.questapplication;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class CreateJoinEventsAdapter extends RecyclerView.Adapter<CreateJoinEventsAdapter.EventViewHolder> {

    private final Context context;
    private List<Event> eventList;

    public CreateJoinEventsAdapter(Context context, List<Event> eventList) {
        this.context = context;
        this.eventList = eventList;
    }

    public void updateList(List<Event> newList) {
        this.eventList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_join_event_card, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);

        holder.eventName.setText(event.getName());
        holder.eventLocation.setText(event.getLocation());
        holder.eventDate.setText(event.getDate());
        holder.eventTime.setText(event.getTime());

        String spotsLeft = context.getString(R.string.spots_left, event.getAvailableSpaces());
        holder.eventSpaces.setText(spotsLeft);

        if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
            holder.eventImage.setVisibility(View.VISIBLE);
            Glide.with(context).load(event.getImageUrl()).into(holder.eventImage);
        } else {
            holder.eventImage.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            Log.d("CreateJoinEventsAdapter", "Event clicked: " + event.getName());

            // Fetch the latest event info before navigating
            FirebaseDatabase.getInstance().getReference("Events")
                    .child(event.getId())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Event updatedEvent = snapshot.getValue(Event.class);
                            if (updatedEvent != null) {
                                Intent intent = new Intent(context, ViewCreatorsEventDetailsActivity.class);
                                intent.putExtra("event", updatedEvent);
                                context.startActivity(intent);
                            } else {
                                Toast.makeText(context, "Event no longer available", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(context, "Failed to load event details", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        ImageView eventImage;
        TextView eventName, eventLocation, eventSpaces, eventDate, eventTime;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventImage = itemView.findViewById(R.id.eventImage);
            eventName = itemView.findViewById(R.id.textEventName);
            eventLocation = itemView.findViewById(R.id.textEventLocation);
            eventSpaces = itemView.findViewById(R.id.textEventSpaces);
            eventDate = itemView.findViewById(R.id.textEventDate);
            eventTime = itemView.findViewById(R.id.textEventTime);
        }
    }
}




