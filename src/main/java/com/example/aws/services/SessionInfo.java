package com.example.aws.services;

public class SessionInfo {
    final private String session;
    final private String accessToken;
    final private String challengeResult;
    
    public SessionInfo(final String session, final String accessToken, final String challengeResult ) {
        this.session = session;
        this.accessToken = accessToken;
        this.challengeResult = challengeResult;
    }

    public String getSession() {
        return session;
    }

    public String getChallengeResult() {
        return challengeResult;
    }

    public String getAccessToken() {
        return accessToken;
    }
    
}
