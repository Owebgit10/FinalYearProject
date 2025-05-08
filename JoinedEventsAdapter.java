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

public class JoinedEventsAdapter extends RecyclerView.Adapter<JoinedEventsAdapter.ViewHolder> {

    private final Context context;
    private final List<Event> events;
    private final List<Integer> spacesBookedList;

    public JoinedEventsAdapter(Context context, List<Event> events, List<Integer> spacesBookedList) {
        this.context = context;
        this.events = events;
        this.spacesBookedList = spacesBookedList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_joined_event_application_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = events.get(position);
        int spacesBooked = spacesBookedList.get(position);

        holder.eventName.setText(event.getName());
        holder.eventLocation.setText(event.getLocation());
        holder.eventDate.setText(event.getDate());
        holder.spacesBooked.setText(
                holder.itemView.getContext().getString(R.string.spaces_booked_label, spacesBooked)
        );


        if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
            holder.eventImage.setVisibility(View.VISIBLE);
            Glide.with(context).load(event.getImageUrl()).into(holder.eventImage);
        } else {
            holder.eventImage.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, JoinedEventApplicationDetailsActivity.class);
            intent.putExtra("event", event);
            intent.putExtra("spacesBooked", spacesBooked);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView eventImage;
        TextView eventName, eventLocation, eventDate, spacesBooked;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            eventImage = itemView.findViewById(R.id.eventImage);
            eventName = itemView.findViewById(R.id.textEventName);
            eventLocation = itemView.findViewById(R.id.textEventLocation);
            eventDate = itemView.findViewById(R.id.textEventDate);
            spacesBooked = itemView.findViewById(R.id.textSpacesBooked);
        }
    }
}


