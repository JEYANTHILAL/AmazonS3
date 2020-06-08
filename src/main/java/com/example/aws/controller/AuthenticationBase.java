package com.example.aws.controller;

import com.example.aws.services.AuthenticationService;

public abstract class AuthenticationBase {
    protected static AuthenticationService authService = new AuthenticationService();
    // minimum length for a user name
    protected final static int USER_NAME_MIN_LENGTH = 4;
    protected final static int PASSWORD_MIN_LENGTH = 8;
    // the name of the session attribute containing user information that is set when the user successfully logs in.
    protected final static String USER_SESSION_ATTR = "user_info";
}
