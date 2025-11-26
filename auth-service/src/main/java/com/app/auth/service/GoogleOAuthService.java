package com.app.auth.service;

import com.app.auth.dto.GoogleLoginRequest;
import com.app.auth.dto.GoogleUserInfo;
import com.app.auth.dto.LoginResponse;

public interface GoogleOAuthService {

    /**
     * Exchange authorization code for access token and get user info
     */
    GoogleUserInfo getUserInfo(String code);

    /**
     * Login or register user with Google account
     */
    LoginResponse loginWithGoogle(GoogleLoginRequest request);
}