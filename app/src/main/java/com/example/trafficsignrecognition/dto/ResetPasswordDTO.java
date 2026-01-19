package com.example.trafficsignrecognition.dto;

public class ResetPasswordDTO {
    private String email;
    private String newPassword;

    // Getters and Setters
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}