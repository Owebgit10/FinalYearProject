package com.example.questapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GoalStatsActivity extends AppCompatActivity {

    private static final String TAG = "GoalStatsActivity";

    private String goalId;
    private String goalTitle;

    private Button checkInButton;

    private ProgressBar goalProgressBar;
    private TextView predictiveDateText;
    private TextView streakCountText;
    private TextView brokenStreakMessage;
    private TextView completionStreakMessage;
    private TextView yourRankMessage;


    private LinearLayout leaderboardList;




    private FirebaseUser currentUser;
    private DatabaseReference checkInRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_stats);

        goalId = getIntent().getStringExtra("goalId");
        goalTitle = getIntent().getStringExtra("goalTitle");

        MaterialToolbar toolbar = findViewById(R.id.statsToolbar);
        toolbar.setTitle(goalTitle);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not signed in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        checkInRef = FirebaseDatabase.getInstance()
                .getReference("UserCheckIns")
                .child(currentUser.getUid())
                .child(goalId);

        checkInButton = findViewById(R.id.checkInButton);
        goalProgressBar = findViewById(R.id.goalProgressBar);
        predictiveDateText = findViewById(R.id.predictiveDateText);

        streakCountText = findViewById(R.id.streakCountText);
        brokenStreakMessage = findViewById(R.id.brokenStreakMessage);
        completionStreakMessage = findViewById(R.id.completionStreakMessage);
        yourRankMessage = findViewById(R.id.yourRankMessage);
        leaderboardList = findViewById(R.id.leaderboardList);

        loadCheckInData();
        loadLeaderboardData();
        calculatePredictedFinishDate();

    }

    private void loadCheckInData() {
        checkInRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String lastDate = snapshot.child("lastCheckInDate").getValue(String.class);
                Long streakCount = snapshot.child("streakCount").getValue(Long.class);
                if (streakCount == null) streakCount = 0L;

                String today = getTodayDate();
                String yesterday = getYesterdayDate();

                if (today.equals(lastDate)) {
                    checkInButton.setEnabled(false);
                    checkInButton.setAlpha(0.5f);
                } else {
                    checkInButton.setEnabled(true);
                    checkInButton.setAlpha(1f);
                }

                if (lastDate != null && !lastDate.equals(today) && !lastDate.equals(yesterday)) {
                    checkInRef.child("streakCount").setValue(0);
                    streakCount = 0L;
                    fetchMotivationAndShowReminder();
                }

                streakCountText.setText(getString(R.string.streak_visual_message, streakCount));
                checkIfGoalCompleteThenCelebrate(streakCount);
                setupCheckInClick(lastDate, streakCount);
                checkIfGoalIsComplete();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GoalStatsActivity.this, "Failed to load check-in", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadLeaderboardData() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot usersSnapshot) {
                List<LeaderboardEntry> leaderboard = new ArrayList<>();
                int totalUsers = (int) usersSnapshot.getChildrenCount();
                int[] finishedCount = {0};

                for (DataSnapshot userSnap : usersSnapshot.getChildren()) {
                    String uid = userSnap.getKey();
                    if (uid == null) continue;

                    String firstName = userSnap.child("firstName").getValue(String.class);
                    String displayName = firstName != null ? firstName : "User";

                    DatabaseReference userCheckInRef = FirebaseDatabase.getInstance().getReference("UserCheckIns").child(uid);
                    DatabaseReference userGoalsRef = FirebaseDatabase.getInstance().getReference("Goals").child(uid);

                    final int[] totalStreak = {0};

                    userCheckInRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot checkInSnapshot) {
                            for (DataSnapshot goalSnap : checkInSnapshot.getChildren()) {
                                Long streak = goalSnap.child("streakCount").getValue(Long.class);
                                if (streak != null) totalStreak[0] += streak;
                            }

                            userGoalsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot goalsSnapshot) {
                                    int totalTasks = 0;
                                    for (DataSnapshot goalSnap : goalsSnapshot.getChildren()) {
                                        DataSnapshot tasks = goalSnap.child("tasks");
                                        for (DataSnapshot task : tasks.getChildren()) {
                                            Boolean completed = task.child("completed").getValue(Boolean.class);
                                            if (completed != null && completed) totalTasks++;
                                        }
                                    }

                                    Log.d(TAG, "User: " + displayName + " | Streak: " + totalStreak[0] + " | Tasks: " + totalTasks);

                                    if (totalStreak[0] + totalTasks > 0) {
                                        leaderboard.add(new LeaderboardEntry(uid, displayName, totalStreak[0], totalTasks));
                                    }

                                    finishedCount[0]++;
                                    if (finishedCount[0] == totalUsers) {
                                        Log.d(TAG, "All users processed. Rendering leaderboard.");
                                        renderLeaderboard(leaderboard);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e(TAG, "Failed to load user goals: " + error.getMessage());
                                    finishedCount[0]++;
                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "Failed to load user check-ins: " + error.getMessage());
                            finishedCount[0]++;
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load users: " + error.getMessage());
            }
        });
    }



    private void calculatePredictedFinishDate() {
        DatabaseReference goalRef = FirebaseDatabase.getInstance()
                .getReference("Goals")
                .child(currentUser.getUid())
                .child(goalId);

        goalRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Goal goal = snapshot.getValue(Goal.class);
                if (goal == null || goal.getTasks() == null || goal.getDeadline() == null) {
                    Log.e(TAG, "Goal data missing.");
                    return;
                }

                int totalTasks = goal.getTasks().size();
                int completedTasks = 0;

                for (Task task : goal.getTasks().values()) {
                    if (task != null && task.isCompleted()) completedTasks++;
                }

                int progressPercent = totalTasks > 0 ? (completedTasks * 100) / totalTasks : 0;

                goalProgressBar.setProgress(progressPercent);

                if (progressPercent == 0) {
                    predictiveDateText.setText(getString(R.string.prediction_start_working));
                    return;
                }

                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    Date deadlineDate = sdf.parse(goal.getDeadline());
                    if (deadlineDate == null) return;

                    long totalDays = (deadlineDate.getTime() - System.currentTimeMillis()) / (1000 * 60 * 60 * 24);
                    if (totalDays <= 0) {
                        predictiveDateText.setText(getString(R.string.prediction_deadline_passed));
                        return;
                    }

                    float pace = (float) completedTasks / (float) totalTasks;
                    long predictedDaysRemaining = (long) ((1 - pace) * totalDays);
                    Date predictedDate = new Date(System.currentTimeMillis() + predictedDaysRemaining * 86400000L);
                    String predictedDateStr = sdf.format(predictedDate);

                    predictiveDateText.setText(getString(R.string.prediction_finish_date, predictedDateStr));
                } catch (Exception e) {
                    Log.e(TAG, "Failed to calculate finish date: " + e.getMessage());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load goal: " + error.getMessage());
            }
        });
    }



    private void renderLeaderboard(List<LeaderboardEntry> leaderboard) {
        Collections.sort(leaderboard, (a, b) -> Integer.compare(b.getTotalScore(), a.getTotalScore()));
        leaderboardList.removeAllViews();
        boolean foundCurrentUser = false;

        for (int i = 0; i < leaderboard.size(); i++) {
            LeaderboardEntry entry = leaderboard.get(i);

            if (i < 5) {
                TextView rankView = new TextView(this);
                String label = String.format(Locale.getDefault(), "%d. %s 🔥 %d pts", i + 1, entry.getDisplayName(), entry.getTotalScore());
                rankView.setText(label);
                rankView.setTextColor(ContextCompat.getColor(this, android.R.color.black));
                rankView.setTextSize(16);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 8, 0, 8);
                rankView.setLayoutParams(params);

                leaderboardList.addView(rankView);
            }

            if (entry.getUserId().equals(currentUser.getUid())) {
                yourRankMessage.setText(getString(R.string.leaderboard_rank_message, i + 1));
                foundCurrentUser = true;
            }
        }

        if (!foundCurrentUser) {
            yourRankMessage.setText(getString(R.string.not_on_leaderboard_message));
        }
    }

    private void setupCheckInClick(String lastDate, long currentStreak) {
        checkInButton.setOnClickListener(v -> {
            String today = getTodayDate();
            String yesterday = getYesterdayDate();

            long newStreak = (yesterday.equals(lastDate)) ? currentStreak + 1 : 1;

            checkInRef.child("lastCheckInDate").setValue(today);
            checkInRef.child("streakCount").setValue(newStreak);

            checkInButton.setEnabled(false);
            checkInButton.setAlpha(0.5f);
            streakCountText.setText(getString(R.string.streak_visual_message, newStreak));

            brokenStreakMessage.setVisibility(View.GONE);
            Toast.makeText(this, "✅ Checked in for today!", Toast.LENGTH_SHORT).show();

            showEnergyLevelSheet(newStreak);
            checkIfGoalCompleteThenCelebrate(newStreak);
        });
    }

    private void showEnergyLevelSheet(long streakCount) {
        ViewGroup root = findViewById(android.R.id.content);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_energy_level, root, false);
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(view);

        String today = getTodayDate();

        View.OnClickListener emojiClickListener = v -> {
            String energy = ((TextView) v).getText().toString();
            DatabaseReference energyRef = FirebaseDatabase.getInstance()
                    .getReference("UserCheckIns")
                    .child(currentUser.getUid())
                    .child(goalId)
                    .child("energyLevels")
                    .child(today);

            energyRef.setValue(energy)
                    .addOnSuccessListener(unused -> Toast.makeText(this, "Energy level saved!", Toast.LENGTH_SHORT).show());

            dialog.dismiss();
        };

        view.findViewById(R.id.emojiTired).setOnClickListener(emojiClickListener);
        view.findViewById(R.id.emojiNeutral).setOnClickListener(emojiClickListener);
        view.findViewById(R.id.emojiGood).setOnClickListener(emojiClickListener);
        view.findViewById(R.id.emojiSuper).setOnClickListener(emojiClickListener);

        dialog.show();
    }

    private void checkIfGoalCompleteThenCelebrate(long streakCount) {
        DatabaseReference tasksRef = FirebaseDatabase.getInstance()
                .getReference("Goals")
                .child(currentUser.getUid())
                .child(goalId)
                .child("tasks");

        tasksRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long total = snapshot.getChildrenCount();
                long completed = 0;

                for (DataSnapshot taskSnap : snapshot.getChildren()) {
                    Boolean completedVal = taskSnap.child("completed").getValue(Boolean.class);
                    if (completedVal != null && completedVal) completed++;
                }

                if (total > 0 && completed == total && streakCount > 0) {
                    completionStreakMessage.setVisibility(View.VISIBLE);
                    completionStreakMessage.setText(getString(R.string.streak_congrats_message, streakCount));
                    checkInButton.setEnabled(false);
                    checkInButton.setAlpha(0.5f);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GoalStatsActivity.this, "Failed to check task completion", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchMotivationAndShowReminder() {
        DatabaseReference motivationRef = FirebaseDatabase.getInstance()
                .getReference("Goals")
                .child(currentUser.getUid())
                .child(goalId)
                .child("motivation");

        motivationRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String motivation = snapshot.getValue(String.class);
                if (motivation == null) motivation = "your goal.";

                brokenStreakMessage.setVisibility(View.VISIBLE);
                brokenStreakMessage.setText(getString(R.string.streak_broken_message, motivation));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GoalStatsActivity.this, "Error loading motivation", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkIfGoalIsComplete() {
        DatabaseReference goalTasksRef = FirebaseDatabase.getInstance()
                .getReference("Goals")
                .child(currentUser.getUid())
                .child(goalId)
                .child("tasks");

        goalTasksRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int total = 0;
                int done = 0;

                for (DataSnapshot taskSnap : snapshot.getChildren()) {
                    total++;
                    Boolean completed = taskSnap.child("completed").getValue(Boolean.class);
                    if (completed != null && completed) done++;
                }

                if (total > 0 && done == total) {
                    checkInButton.setEnabled(false);
                    checkInButton.setAlpha(0.5f);
                    checkInButton.setText(getString(R.string.goal_completed));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GoalStatsActivity.this, "Failed to load goal data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getTodayDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    private String getYesterdayDate() {
        long yesterdayMillis = System.currentTimeMillis() - 86400000L;
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(yesterdayMillis));
    }
}


