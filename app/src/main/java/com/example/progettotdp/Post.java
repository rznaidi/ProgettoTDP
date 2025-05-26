package com.example.progettotdp;

public class Post {
    private String username;
    private String description;
    private String location;
    private String imageUrl;

    public Post(String username, String description, String location, String imageUrl) {
        this.username = username;
        this.description = description;
        this.location = location;
        this.imageUrl = imageUrl;
    }

    public String getUsername() {
        return username;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}