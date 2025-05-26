package com.example.progettotdp;

public class Post {
    private int id;
    private String username;
    private String description;
    private String location;
    private String imageUrl;

    public Post(int id, String username, String description, String location, String imageUrl) {
        this.id = id;
        this.username = username;
        this.description = description;
        this.location = location;
        this.imageUrl = imageUrl;
    }

    // Getter
    public int getId() {
        return id;
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

    // Setter
    public void setId(int id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
