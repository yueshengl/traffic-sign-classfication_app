package com.example.trafficsignrecognition.event;

public class UsernameChangedEvent {
    private final String username;

    public UsernameChangedEvent(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
