package com.example.questapplication;

import androidx.core.content.ContextCompat;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CreateGoalActivity extends AppCompatActivity {

    private EditText goalTitleInput, motivationInput, goalDeadlineInput;
    private LinearLayout taskInputContainer;
    private Button addTaskButton, btnCreateGoal, btnCancelGoal;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_goal);

        Toolbar toolbar = findViewById(R.id.toolbarCreateGoal);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Create a Goal");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        goalTitleInput = findViewById(R.id.goalTitleInput);
        motivationInput = findViewById(R.id.motivationInput);
        goalDeadlineInput = findViewById(R.id.goalDeadlineInput);
        taskInputContainer = findViewById(R.id.taskInputContainer);
        addTaskButton = findViewById(R.id.addTaskButton);
        btnCreateGoal = findViewById(R.id.btnCreateGoal);
        btnCancelGoal = findViewById(R.id.btnCancelGoal);

        goalDeadlineInput.setOnClickListener(v -> openDatePicker());
        addTaskButton.setOnClickListener(v -> addTaskField());
        btnCreateGoal.setOnClickListener(v -> createGoal());
        btnCancelGoal.setOnClickListener(v -> finish());

        addTaskField(); // Add first task field automatically
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

    private void addTaskField() {
        EditText taskInput = new EditText(this);
        taskInput.setHint("Add tasks for this goal");
        taskInput.setBackgroundResource(R.drawable.rounded_edittext);
        taskInput.setPadding(24, 24, 24, 24);
        taskInput.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        taskInput.setHintTextColor(ContextCompat.getColor(this, R.color.gray));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 16, 0, 0);
        taskInput.setLayoutParams(params);
        taskInputContainer.addView(taskInput);
    }

    private void createGoal() {
        String title = goalTitleInput.getText().toString().trim();
        String motivation = motivationInput.getText().toString().trim();
        String deadline = goalDeadlineInput.getText().toString().trim();

        if (title.isEmpty() || deadline.isEmpty()) {
            Toast.makeText(this, "Please complete all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Task> tasksMap = new HashMap<>();
        int taskCount = 0;

        for (int i = 0; i < taskInputContainer.getChildCount(); i++) {
            View child = taskInputContainer.getChildAt(i);
            if (child instanceof EditText) {
                String taskTitle = ((EditText) child).getText().toString().trim();
                if (!TextUtils.isEmpty(taskTitle)) {
                    String taskId = FirebaseDatabase.getInstance().getReference().push().getKey();
                    tasksMap.put(taskId, new Task(taskTitle, false));
                    taskCount++;
                }
            }
        }

        if (taskCount == 0) {
            Toast.makeText(this, "Please add at least one task.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You must be signed in.", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = currentUser.getUid();

        String goalId = FirebaseDatabase.getInstance().getReference("Goals").push().getKey();
        if (goalId == null) {
            Toast.makeText(this, "Error creating goal ID. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        Goal goal = new Goal(goalId, userId, title, deadline, motivation, null);
        goal.setTasks(tasksMap); // titleSearch was already set via constructor

        FirebaseDatabase.getInstance().getReference("Goals")
                .child(userId)
                .child(goalId)
                .setValue(goal)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(CreateGoalActivity.this, "Goal created successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CreateGoalActivity.this, "Failed to create goal.", Toast.LENGTH_SHORT).show();
                });

    }
}




