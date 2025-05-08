package com.example.questapplication;

import java.util.Map;

public class Goal {
    private String goalId;
    private String userId;
    private String title;
    private String titleSearch; // 🔍 For lowercase comparison
    private String deadline;
    private String motivation;
    private Map<String, Task> tasks;

    public Goal() {
        // Required for Firebase
    }

    public Goal(String goalId, String userId, String title, String deadline, String motivation, Map<String, Task> tasks) {
        this.goalId = goalId;
        this.userId = userId;
        setTitle(title); // Ensures titleSearch is set
        this.deadline = deadline;
        this.motivation = motivation;
        this.tasks = tasks;
    }

    public String getGoalId() {
        return goalId;
    }

    public String getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public String getTitleSearch() {
        return titleSearch;
    }

    public String getDeadline() {
        return deadline;
    }

    public String getMotivation() {
        return motivation;
    }

    public Map<String, Task> getTasks() {
        return tasks;
    }

    public void setTitle(String title) {
        this.title = title;
        this.titleSearch = title != null ? title.toLowerCase() : null;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public void setMotivation(String motivation) {
        this.motivation = motivation;
    }

    public void setTasks(Map<String, Task> tasks) {
        this.tasks = tasks;
    }
}



