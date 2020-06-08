package com.example.aws.services;

public class LoginInfo extends UserInfo {
    private Boolean newPasswordRequired = false;
    
    public LoginInfo(final String userName, final String email) {
        super(userName, email);
    }
    
    public LoginInfo(final UserInfo info) {
        this(info.getUserName(), info.getEmailAddr());
    }

	public Boolean getNewPasswordRequired() {
        return newPasswordRequired;
    }

    public void setNewPasswordRequired(Boolean newPasswordRequired) {
        this.newPasswordRequired = newPasswordRequired;
    }
    
}
