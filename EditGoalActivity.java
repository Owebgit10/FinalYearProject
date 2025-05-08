package com.example.questapplication;

import androidx.annotation.NonNull;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditGoalActivity extends AppCompatActivity {

    private EditText goalTitleInput, motivationInput, goalDeadlineInput;
    private LinearLayout taskInputContainer;
    private Button addTaskButton, btnUpdateGoal, btnCancelEditGoal, deleteGoalButton;
    private String goalId;
    private Goal currentGoal;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_goal);

        Toolbar toolbar = findViewById(R.id.toolbarEditGoal);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Edit Goal");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        goalTitleInput = findViewById(R.id.goalTitleInput);
        motivationInput = findViewById(R.id.motivationInput);
        goalDeadlineInput = findViewById(R.id.goalDeadlineInput);
        taskInputContainer = findViewById(R.id.taskInputContainer);
        addTaskButton = findViewById(R.id.addTaskButton);
        btnUpdateGoal = findViewById(R.id.btnUpdateGoal);
        btnCancelEditGoal = findViewById(R.id.btnCancelEditGoal);
        deleteGoalButton = findViewById(R.id.deleteGoalButton);

        goalDeadlineInput.setOnClickListener(v -> openDatePicker());
        addTaskButton.setOnClickListener(v -> addTaskField(null));
        btnUpdateGoal.setOnClickListener(v -> updateGoal());
        btnCancelEditGoal.setOnClickListener(v -> finish());
        deleteGoalButton.setOnClickListener(v -> confirmAndDeleteGoal());

        goalId = getIntent().getStringExtra("goalId");
        if (goalId != null) {
            loadGoalData(goalId);
        }
    }

    private void openDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String formattedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            goalDeadlineInput.setText(formattedDate);
        },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        dialog.getDatePicker().setMinDate(System.currentTimeMillis());
        dialog.show();
    }

    private void loadGoalData(String goalId) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "You must be signed in.", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();
        DatabaseReference goalRef = FirebaseDatabase.getInstance().getReference("Goals").child(uid).child(goalId);

        goalRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentGoal = snapshot.getValue(Goal.class);
                if (currentGoal != null) {
                    goalTitleInput.setText(currentGoal.getTitle());
                    goalDeadlineInput.setText(currentGoal.getDeadline());
                    motivationInput.setText(currentGoal.getMotivation());

                    taskInputContainer.removeAllViews();
                    if (currentGoal.getTasks() != null) {
                        for (Task task : currentGoal.getTasks().values()) {
                            addTaskField(task.getTitle());
                        }
                    } else {
                        addTaskField(null);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditGoalActivity.this, "Failed to load goal.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void addTaskField(@Nullable String prefillText) {
        LinearLayout taskRow = new LinearLayout(this);
        taskRow.setOrientation(LinearLayout.HORIZONTAL);

        EditText taskInput = new EditText(this);
        taskInput.setHint("Task");
        taskInput.setBackgroundResource(R.drawable.rounded_edittext);
        taskInput.setPadding(24, 24, 24, 24);
        taskInput.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        taskInput.setHintTextColor(ContextCompat.getColor(this, R.color.gray));

        if (prefillText != null) {
            taskInput.setText(prefillText);
        }

        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        taskInput.setLayoutParams(inputParams);

        ImageButton deleteButton = new ImageButton(this);
        deleteButton.setImageResource(R.drawable.ic_closetag);
        deleteButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
        deleteButton.setOnClickListener(v -> taskInputContainer.removeView(taskRow));

        taskRow.addView(taskInput);
        taskRow.addView(deleteButton);

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(0, 16, 0, 0);
        taskRow.setLayoutParams(rowParams);

        taskInputContainer.addView(taskRow);
    }

    private void updateGoal() {
        String title = goalTitleInput.getText().toString().trim();
        String deadline = goalDeadlineInput.getText().toString().trim();
        String motivation = motivationInput.getText().toString().trim();

        if (title.isEmpty() || deadline.isEmpty()) {
            Toast.makeText(this, "Please complete all required fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Task> updatedTasks = new HashMap<>();
        for (int i = 0; i < taskInputContainer.getChildCount(); i++) {
            LinearLayout row = (LinearLayout) taskInputContainer.getChildAt(i);
            EditText taskInput = (EditText) row.getChildAt(0);
            String taskText = taskInput.getText().toString().trim();

            if (!taskText.isEmpty()) {
                String taskId = FirebaseDatabase.getInstance().getReference().push().getKey();
                updatedTasks.put(taskId, new Task(taskText, false));
            }
        }

        if (updatedTasks.isEmpty()) {
            Toast.makeText(this, "Add at least one task.", Toast.LENGTH_SHORT).show();
            return;
        }

        currentGoal.setTitle(title);
        currentGoal.setDeadline(deadline);
        currentGoal.setMotivation(motivation);
        currentGoal.setTasks(updatedTasks);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "You must be signed in.", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();

        FirebaseDatabase.getInstance().getReference("Goals")
                .child(uid)
                .child(goalId)
                .setValue(currentGoal)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Goal updated successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update goal.", Toast.LENGTH_SHORT).show());
    }

    private void confirmAndDeleteGoal() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Goal")
                .setMessage("Are you sure you want to delete this goal?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user == null) {
                        Toast.makeText(this, "You must be signed in.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String uid = user.getUid();

                    FirebaseDatabase.getInstance().getReference("Goals")
                            .child(uid)
                            .child(goalId)
                            .removeValue()
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Goal deleted successfully!", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to delete goal.", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }
}




