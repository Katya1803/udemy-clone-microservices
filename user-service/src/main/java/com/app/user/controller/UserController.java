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

        log.debug("Fetching current user: {}", accountId);

        UserResponse response = userService.getUserByAccountId(accountId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get user by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable String id) {

        log.debug("Fetching user by ID: {}", id);

        UserResponse response = userService.getUserByAccountId(id);

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
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable String id,
            @Valid @RequestBody UpdateUserRequest request,
            @CurrentAccount String currentUserId) {

        log.info("Updating user: {}", id);

        // Only allow users to update their own info
        if (!id.equals(currentUserId)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("You can only update your own information"));
        }

        UserResponse response = userService.updateUser(id, request);

        return ResponseEntity.ok(ApiResponse.success(response, "User updated successfully"));
    }

    /**
     * Delete user (soft delete)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable String id) {

        log.info("Deleting user: {}", id);

        userService.deleteUser(id);

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