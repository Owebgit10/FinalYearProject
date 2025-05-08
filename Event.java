package com.example.questapplication;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Event implements Serializable {
    private String id;
    private String name;
    private String nameSearch;

    private String location;
    private String locationSearch;

    private String date;
    private String time;
    private String description;
    private int availableSpaces;

    private String category;
    private String categorySearch;

    private List<String> tags;         // original tags
    private List<String> tagsSearch;   // lowercased tags for search

    private String userId;
    private String imageUrl;

    private double latitude;
    private double longitude;

    // ✅ Used only for local filtering/sorting – not saved in Firebase
    private transient int relevanceScore;

    public Event() {
        // Default constructor required for Firebase
    }

    public Event(String id, String name, String location, String date, String time, String description,
                 int availableSpaces, String category, List<String> tags, String userId, String imageUrl,
                 double latitude, double longitude) {
        this.id = id;

        setName(name);
        setLocation(location);
        this.date = date;
        this.time = time;
        this.description = description;
        this.availableSpaces = availableSpaces;

        setCategory(category);
        setTags(tags);

        this.userId = userId;
        this.imageUrl = imageUrl;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) {
        this.name = name;
        this.nameSearch = name != null ? name.toLowerCase() : null;
    }

    public String getNameSearch() { return nameSearch; }

    public String getLocation() { return location; }
    public void setLocation(String location) {
        this.location = location;
        this.locationSearch = location != null ? location.toLowerCase() : null;
    }

    public String getLocationSearch() { return locationSearch; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getAvailableSpaces() { return availableSpaces; }
    public void setAvailableSpaces(int availableSpaces) { this.availableSpaces = availableSpaces; }

    public String getCategory() { return category; }
    public void setCategory(String category) {
        this.category = category;
        this.categorySearch = category != null ? category.toLowerCase() : null;
    }

    public String getCategorySearch() { return categorySearch; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) {
        this.tags = tags;

        if (tags != null) {
            List<String> lowerTags = new ArrayList<>();
            for (String tag : tags) {
                if (tag != null) lowerTags.add(tag.toLowerCase());
            }
            this.tagsSearch = lowerTags;
        } else {
            this.tagsSearch = null;
        }
    }

    public List<String> getTagsSearch() { return tagsSearch; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    // ✅ Relevance Score – used for sorting search results
    public int getRelevanceScore() { return relevanceScore; }
    public void setRelevanceScore(int relevanceScore) { this.relevanceScore = relevanceScore; }
}






