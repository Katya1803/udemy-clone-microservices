package com.app.user.service;

import com.app.user.dto.UpdateUserProfileRequest;
import com.app.user.dto.UserProfileResponse;

public interface UserProfileService {

    UserProfileResponse getProfile(String userId);

    UserProfileResponse updateProfile(String userId, UpdateUserProfileRequest request);
}