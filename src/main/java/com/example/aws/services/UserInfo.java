package com.example.aws.services;

public class UserInfo {
    private final String userName;
    private final String emailAddr;
    
    public UserInfo(final String userName, final String emailAddr) {
        this.userName = userName;
        this.emailAddr = emailAddr;
    }

    public String getUserName() {
        return userName;
    }

    public String getEmailAddr() {
        return emailAddr;
    }


}
