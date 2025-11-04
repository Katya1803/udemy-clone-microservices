package com.app.user.service;

import com.app.user.dto.CreateUserRequest;
import com.app.user.dto.UpdateUserRequest;
import com.app.user.dto.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserResponse createUser(CreateUserRequest request);

    /**
     * Get user by accountId (from auth service)
     */
    UserResponse getUserByAccountId(String accountId);

    /**
     * Get user by userId (internal user-service ID)
     */
    UserResponse getUserByUserId(String userId);

    UserResponse getUserByUsername(String username);

    Page<UserResponse> getAllUsers(Pageable pageable);

    /**
     * Update user by userId, verify ownership with accountId
     */
    UserResponse updateUser(String userId, UpdateUserRequest request, String accountId);

    void deleteUser(String userId);
}