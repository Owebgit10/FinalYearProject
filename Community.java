package com.example.questapplication;

import java.io.Serializable;
import java.util.List;

public class Community implements Serializable {
    private String id;
    private String name;
    private String nameSearch;
    private String description;
    private List<String> tags;
    private List<String> rules;
    private String userId;
    private String imageUrl;
    private long createdAt;

    public Community() {}

    public Community(String id, String name, String description, List<String> tags, List<String> rules,
                     String userId, String imageUrl, long createdAt) {
        this.id = id;
        this.name = name;
        this.nameSearch = name.toLowerCase();
        this.description = description;
        this.tags = tags;
        this.rules = rules;
        this.userId = userId;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getNameSearch() { return nameSearch; }
    public String getDescription() { return description; }
    public List<String> getTags() { return tags; }
    public List<String> getRules() { return rules; }
    public String getUserId() { return userId; }
    public String getImageUrl() { return imageUrl; }
    public long getCreatedAt() { return createdAt; }
}


