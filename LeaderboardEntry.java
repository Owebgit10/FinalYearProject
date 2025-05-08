package com.example.questapplication;

public class LeaderboardEntry {
    private String userId;
    private String displayName;
    private int checkIns;
    private int completedTasks;
    private int totalScore;

    public LeaderboardEntry() {
        // Default constructor required for Firebase
    }

    public LeaderboardEntry(String userId, String displayName, int checkIns, int completedTasks) {
        this.userId = userId;
        this.displayName = displayName;
        this.checkIns = checkIns;
        this.completedTasks = completedTasks;
        this.totalScore = checkIns + completedTasks;
    }

    public String getUserId() {
        return userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getCheckIns() {
        return checkIns;
    }

    public int getCompletedTasks() {
        return completedTasks;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setCheckIns(int checkIns) {
        this.checkIns = checkIns;
        updateTotalScore();
    }

    public void setCompletedTasks(int completedTasks) {
        this.completedTasks = completedTasks;
        updateTotalScore();
    }

    private void updateTotalScore() {
        this.totalScore = this.checkIns + this.completedTasks;
    }
}

