package com.example.questapplication;

public class Task {
    private String title;
    private String titleSearch; // 🔍 Lowercase version
    private boolean completed;

    public Task() {
        // Required for Firebase
    }

    public Task(String title, boolean completed) {
        setTitle(title); // Ensures titleSearch is set
        this.completed = completed;
    }

    public String getTitle() {
        return title;
    }

    public String getTitleSearch() {
        return titleSearch;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setTitle(String title) {
        this.title = title;
        this.titleSearch = title != null ? title.toLowerCase() : null;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}


