package com.app.user.controller;

import com.app.common.dto.response.ApiResponse;
import com.app.common.util.CurrentAccount;
import com.app.user.dto.UpdateUserProfileRequest;
import com.app.user.dto.UserProfileResponse;
import com.app.user.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/users/{userId}/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService profileService;

    /**
     * Get user profile by userId
     */
    @GetMapping
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(
            @PathVariable String userId) {

        log.debug("Fetching profile for userId: {}", userId);

        UserProfileResponse response = profileService.getProfileByUserId(userId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get current user profile
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(
            @CurrentAccount String accountId) {

        log.debug("Fetching current user profile for accountId: {}", accountId);

        UserProfileResponse response = profileService.getProfileByAccountId(accountId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Update user profile
     */
    @PutMapping
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @PathVariable String userId,
            @Valid @RequestBody UpdateUserProfileRequest request,
            @CurrentAccount String accountId) {

        log.info("Updating profile for userId: {} by accountId: {}", userId, accountId);

        UserProfileResponse response = profileService.updateProfileByUserId(userId, request, accountId);

        return ResponseEntity.ok(ApiResponse.success(response, "Profile updated successfully"));
    }
}