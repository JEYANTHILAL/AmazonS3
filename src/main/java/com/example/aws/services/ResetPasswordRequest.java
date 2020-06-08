package com.example.aws.services;

public class ResetPasswordRequest {
    private final String userName;
    private final String resetCode;
    private final String newPassword;
    
    public ResetPasswordRequest(final String userName, final String resetCode, final String newPassword) {
        this.userName = userName;
        this.resetCode = resetCode;
        this.newPassword = newPassword;
    }

    public String getUserName() {
        return userName;
    }

    public String getResetCode() {
        return resetCode;
    }

    public String getNewPassword() {
        return newPassword;
    }
    
}
