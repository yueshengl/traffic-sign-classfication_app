package com.example.trafficsignrecognition.event;

public class ImagePathChangedEvent {
    private final String imagePath;

    public ImagePathChangedEvent(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getImagePath() {
        return imagePath;
    }
}
