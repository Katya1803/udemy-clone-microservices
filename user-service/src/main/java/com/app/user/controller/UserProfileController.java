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
     * Get user profile
     */
    @GetMapping
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(
            @PathVariable String userId) {

        log.debug("Fetching profile for user: {}", userId);

        UserProfileResponse response = profileService.getProfile(userId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get current user profile
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(
            @CurrentAccount String userId) {

        log.debug("Fetching current user profile: {}", userId);

        UserProfileResponse response = profileService.getProfile(userId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Update user profile
     */
    @PutMapping
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @PathVariable String userId,
            @Valid @RequestBody UpdateUserProfileRequest request,
            @CurrentAccount String currentUserId) {

        log.info("Updating profile for user: {}", userId);

        if (!userId.equals(currentUserId)) {
            return ResponseEntity
                    .status(403)
                    .body(ApiResponse.error("You can only update your own profile"));
        }

        UserProfileResponse response = profileService.updateProfile(userId, request);

        return ResponseEntity.ok(ApiResponse.success(response, "Profile updated successfully"));
    }
}