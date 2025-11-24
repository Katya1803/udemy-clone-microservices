package com.app.user.controller;

import com.app.common.dto.response.ApiResponse;
import com.app.common.dto.response.PageResponse;
import com.app.common.util.CurrentAccount;
import com.app.user.dto.CreateUserRequest;
import com.app.user.dto.UpdateUserRequest;
import com.app.user.dto.UserResponse;
import com.app.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Create user - Called by auth-service (service-to-service)
     */
    @PostMapping
    @PreAuthorize("hasRole('SERVICE')")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request) {

        log.info("Creating user: {}", request.getUsername());

        UserResponse response = userService.createUser(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "User created successfully"));
    }

    /**
     * Get current user info
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @CurrentAccount String accountId) {

        log.debug("Fetching current user by accountId: {}", accountId);

        UserResponse response = userService.getUserByAccountId(accountId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get user by accountId (service-to-service)
     * Used by blog-service to fetch username when creating posts
     */
    @GetMapping("/account/{accountId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByAccountId(
            @PathVariable String accountId) {

        log.debug("Fetching user by accountId: {}", accountId);

        UserResponse response = userService.getUserByAccountId(accountId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get user by userId (internal ID)
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable String userId) {

        log.debug("Fetching user by userId: {}", userId);

        UserResponse response = userService.getUserByUserId(userId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get user by username
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByUsername(
            @PathVariable String username) {

        log.debug("Fetching user by username: {}", username);

        UserResponse response = userService.getUserByUsername(username);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get all users (paginated)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        log.debug("Fetching all users, page: {}, size: {}", page, size);

        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<UserResponse> users = userService.getAllUsers(pageable);
        PageResponse<UserResponse> pageResponse = PageResponse.of(users);

        return ResponseEntity.ok(ApiResponse.success(pageResponse));
    }

    /**
     * Update user
     */
    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable String userId,
            @Valid @RequestBody UpdateUserRequest request,
            @CurrentAccount String accountId) {

        log.info("Updating user: {} by accountId: {}", userId, accountId);

        UserResponse response = userService.updateUser(userId, request, accountId);

        return ResponseEntity.ok(ApiResponse.success(response, "User updated successfully"));
    }

    /**
     * Delete user (soft delete)
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable String userId) {

        log.info("Deleting user: {}", userId);

        userService.deleteUser(userId);

        return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
    }

    /**
     * Health check
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("User service is running"));
    }
}