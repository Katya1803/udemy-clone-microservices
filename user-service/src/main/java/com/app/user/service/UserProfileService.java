package com.app.user.service;

import com.app.user.dto.UpdateUserProfileRequest;
import com.app.user.dto.UserProfileResponse;

public interface UserProfileService {

    /**
     * Get profile by accountId (from auth service)
     */
    UserProfileResponse getProfileByAccountId(String accountId);

    /**
     * Get profile by userId (internal user-service ID)
     */
    UserProfileResponse getProfileByUserId(String userId);

    /**
     * Update profile by userId, verify ownership with accountId
     */
    UserProfileResponse updateProfileByUserId(String userId, UpdateUserProfileRequest request, String accountId);
}