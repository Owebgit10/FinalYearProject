package com.example.questapplication;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GoalAdapter extends RecyclerView.Adapter<GoalAdapter.GoalViewHolder> {

    private final List<Goal> goalList;

    public GoalAdapter(List<Goal> goalList) {
        this.goalList = goalList;
    }

    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.goal_item, parent, false);
        return new GoalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GoalViewHolder holder, int position) {
        Goal goal = goalList.get(position);
        holder.bind(goal);
    }

    @Override
    public int getItemCount() {
        return goalList.size();
    }

    public static class GoalViewHolder extends RecyclerView.ViewHolder {

        private final TextView goalTitleText, goalDeadlineText, goalProgressText, overdueWarningText, completedBadge;
        private final LinearLayout taskListContainer;
        private final View progressFill;
        private final CardView goalCard;
        private final ImageButton editGoalButton;
        private boolean isExpanded = false;
        private final Button viewStatsButton;




        public GoalViewHolder(@NonNull View itemView) {
            super(itemView);

            goalTitleText = itemView.findViewById(R.id.goalTitleText);
            goalDeadlineText = itemView.findViewById(R.id.goalDeadlineText);
            goalProgressText = itemView.findViewById(R.id.goalProgressText);
            overdueWarningText = itemView.findViewById(R.id.overdueWarningText);
            completedBadge = itemView.findViewById(R.id.completedBadge);
            progressFill = itemView.findViewById(R.id.progressFill);
            taskListContainer = itemView.findViewById(R.id.taskListContainer);
            goalCard = itemView.findViewById(R.id.goalCard);
            editGoalButton = itemView.findViewById(R.id.editGoalButton);
            viewStatsButton = itemView.findViewById(R.id.viewStatsButton);


            goalCard.setOnClickListener(v -> toggleExpandCollapse());
        }

        public void bind(Goal goal) {
            goalTitleText.setText(goal.getTitle());
            goalDeadlineText.setText(itemView.getContext().getString(R.string.deadline_format, goal.getDeadline()));

            int totalTasks = (goal.getTasks() != null) ? goal.getTasks().size() : 0;
            int completedTasks = 0;

            if (goal.getTasks() != null) {
                for (Task task : goal.getTasks().values()) {
                    if (task.isCompleted()) {
                        completedTasks++;
                    }
                }
            }

            int progressPercent = (totalTasks > 0) ? (completedTasks * 100) / totalTasks : 0;
            goalProgressText.setText(itemView.getContext().getString(R.string.progress_percentage, progressPercent));
            animateProgressFill(progressPercent);

            completedBadge.setVisibility(progressPercent == 100 ? View.VISIBLE : View.GONE);
            overdueWarningText.setVisibility((progressPercent < 100 && isGoalOverdue(goal.getDeadline())) ? View.VISIBLE : View.GONE);

            int color;
            if (progressPercent == 100) {
                color = itemView.getContext().getColor(R.color.green);
            } else if (progressPercent >= 50) {
                color = itemView.getContext().getColor(R.color.gold);
            } else {
                color = itemView.getContext().getColor(R.color.gray);
            }
            goalCard.setCardBackgroundColor(color);

            setupTaskList(goal);
            setupEditButton(goal);

            viewStatsButton.setOnClickListener(v -> {
                Intent intent = new Intent(itemView.getContext(), GoalStatsActivity.class);
                intent.putExtra("goalId", goal.getGoalId());
                intent.putExtra("goalTitle", goal.getTitle());
                itemView.getContext().startActivity(intent);
            });

        }

        private void setupEditButton(Goal goal) {
            editGoalButton.setOnClickListener(v -> {
                Intent intent = new Intent(itemView.getContext(), EditGoalActivity.class);
                intent.putExtra("goalId", goal.getGoalId());
                itemView.getContext().startActivity(intent);
            });
        }

        private void setupTaskList(Goal goal) {
            taskListContainer.removeAllViews();
            if (goal.getTasks() == null) return;

            for (Map.Entry<String, Task> entry : goal.getTasks().entrySet()) {
                String taskId = entry.getKey();
                Task task = entry.getValue();

                CheckBox checkBox = new CheckBox(itemView.getContext());
                checkBox.setText(task.getTitle());
                checkBox.setChecked(task.isCompleted());

                checkBox.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.black));
                checkBox.setButtonTintList(ContextCompat.getColorStateList(itemView.getContext(), R.color.checkbox_green));

                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    task.setCompleted(isChecked);
                    updateGoalProgress(goal);

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user == null) return;

                    FirebaseDatabase.getInstance().getReference("Goals")
                            .child(user.getUid())
                            .child(goal.getGoalId())
                            .child("tasks")
                            .child(taskId)
                            .child("completed")
                            .setValue(isChecked);
                });

                taskListContainer.addView(checkBox);
            }
        }

        private void updateGoalProgress(Goal goal) {
            int totalTasks = (goal.getTasks() != null) ? goal.getTasks().size() : 0;
            int completedTasks = 0;

            if (goal.getTasks() != null) {
                for (Task task : goal.getTasks().values()) {
                    if (task.isCompleted()) {
                        completedTasks++;
                    }
                }
            }

            int progressPercent = (totalTasks > 0) ? (completedTasks * 100) / totalTasks : 0;
            goalProgressText.setText(itemView.getContext().getString(R.string.progress_percentage, progressPercent));
            animateProgressFill(progressPercent);

            completedBadge.setVisibility(progressPercent == 100 ? View.VISIBLE : View.GONE);
            overdueWarningText.setVisibility((progressPercent < 100 && isGoalOverdue(goal.getDeadline())) ? View.VISIBLE : View.GONE);

            int color;
            if (progressPercent == 100) {
                color = itemView.getContext().getColor(R.color.green);
            } else if (progressPercent >= 50) {
                color = itemView.getContext().getColor(R.color.gold);
            } else {
                color = itemView.getContext().getColor(R.color.gray);
            }
            goalCard.setCardBackgroundColor(color);
        }

        private boolean isGoalOverdue(String deadline) {
            if (deadline == null || deadline.isEmpty()) return false;
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date deadlineDate = sdf.parse(deadline);
                return deadlineDate != null && deadlineDate.before(new Date());
            } catch (Exception e) {
                Log.e("GoalAdapter", "Failed to parse deadline: " + deadline, e);
                return false;
            }
        }

        private void animateProgressFill(int progressPercent) {
            View parentView = (View) progressFill.getParent();
            int cardWidth = parentView.getWidth();

            if (cardWidth == 0) {
                progressFill.post(() -> animateProgressFill(progressPercent));
                return;
            }

            int targetWidth = (int) (cardWidth * (progressPercent / 100f));

            ValueAnimator animator = ValueAnimator.ofInt(progressFill.getWidth(), targetWidth);
            animator.setDuration(500);
            animator.addUpdateListener(animation -> {
                int animatedWidth = (int) animation.getAnimatedValue();
                ViewGroup.LayoutParams params = progressFill.getLayoutParams();
                params.width = animatedWidth;
                progressFill.setLayoutParams(params);
            });
            animator.start();
        }

        private void toggleExpandCollapse() {
            boolean wasExpanded = isExpanded;
            isExpanded = !wasExpanded;

            taskListContainer.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            viewStatsButton.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        }

    }
}










