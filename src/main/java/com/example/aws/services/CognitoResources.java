package com.example.aws.services;

import com.amazonaws.regions.Regions;


public interface CognitoResources {
    public final static String EMAIL = "email";
    // Cognito IAM ID for full Cognito access
    public final static String cognitoID = "********************";
    // Cognito IAM "secret key" for full Cognito access
    public final static String cognitoKey = "**********";
    public final static String poolID = "*******************";
    public final static String clientID = "********************";
    // Replace this with the AWS region for your application
    public final static Regions region = Regions.US_WEST_2;
}
