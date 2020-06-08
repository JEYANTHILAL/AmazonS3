package com.example.aws.services;

public class PasswordRequest {
    private final String userName;
    private final String oldPassword;
    private final String newPassword;
    
    public PasswordRequest(final String userName, final String oldPassword, final String newPassword) {
        this.userName = userName;
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }

    public String getUserName() {
        return userName;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }
    
}
