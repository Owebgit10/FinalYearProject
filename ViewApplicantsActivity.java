package com.example.questapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ViewApplicantsActivity extends AppCompatActivity {

    private RecyclerView applicantsRecyclerView;
    private TextView emptyMessage;
    private ApplicantAdapter adapter;
    private final List<Applicant> applicantList = new ArrayList<>();
    private String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_applicants);

        // Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbarApplicants);
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        applicantsRecyclerView = findViewById(R.id.applicantsRecyclerView);
        emptyMessage = findViewById(R.id.noApplicantsText);

        adapter = new ApplicantAdapter(this, applicantList);
        applicantsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        applicantsRecyclerView.setAdapter(adapter);

        eventId = getIntent().getStringExtra("eventId");
        if (eventId != null) {
            loadApplicants();
        }
    }

    private void loadApplicants() {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("EventApplications")
                .child(eventId); // ✅ Correct: full applicant data is under EventApplications

        ref.addValueEventListener(new ValueEventListener() { // ✅ Live updates
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                applicantList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Applicant applicant = child.getValue(Applicant.class);
                    if (applicant != null) {
                        applicantList.add(applicant);
                    }
                }

                adapter.notifyDataSetChanged();
                emptyMessage.setVisibility(applicantList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ViewApplicants", "Failed to load applicants", error.toException());
            }
        });
    }
}


