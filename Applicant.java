package com.example.questapplication;

public class Applicant {

    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String message;
    private int spacesRequested;
    private long timestamp;

    // Required empty constructor for Firebase
    public Applicant() {}

    public Applicant(String firstName, String lastName, String email, String phone, String message, int spacesRequested, long timestamp) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.message = message;
        this.spacesRequested = spacesRequested;
        this.timestamp = timestamp;
    }

    // Getters
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getMessage() { return message; }
    public int getSpacesRequested() { return spacesRequested; }
    public long getTimestamp() { return timestamp; }

    // Setters (optional, depending on your use case)
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setMessage(String message) { this.message = message; }
    public void setSpacesRequested(int spacesRequested) { this.spacesRequested = spacesRequested; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}

