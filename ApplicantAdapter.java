package com.example.questapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ApplicantAdapter extends RecyclerView.Adapter<ApplicantAdapter.ViewHolder> {

    private final Context context;
    private final List<Applicant> applicantList;

    public ApplicantAdapter(Context context, List<Applicant> applicantList) {
        this.context = context;
        this.applicantList = applicantList;
    }

    @NonNull
    @Override
    public ApplicantAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_applicant_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Applicant applicant = applicantList.get(position);
        holder.name.setText(context.getString(R.string.applicant_full_name,
                applicant.getFirstName(), applicant.getLastName()));

        holder.email.setText(applicant.getEmail());
        holder.phone.setText(applicant.getPhone());
        holder.spaces.setText(context.getString(R.string.requested_spaces,
                applicant.getSpacesRequested()));

        String msg = applicant.getMessage().isEmpty()
                ? context.getString(R.string.no_message_provided)
                : applicant.getMessage();

        holder.message.setText(context.getString(R.string.message_with_content, msg));

    }

    @Override
    public int getItemCount() {
        return applicantList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, email, phone, spaces, message;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.applicantName);
            email = itemView.findViewById(R.id.applicantEmail);
            phone = itemView.findViewById(R.id.applicantPhone);
            spaces = itemView.findViewById(R.id.applicantSpaces);
            message = itemView.findViewById(R.id.applicantMessage);
        }
    }
}


