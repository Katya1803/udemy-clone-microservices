package com.app.user.service.impl;

import com.app.common.exception.ResourceNotFoundException;
import com.app.user.constant.UserStatus;
import com.app.user.dto.CreateUserRequest;
import com.app.user.dto.UpdateUserRequest;
import com.app.user.dto.UserProfileResponse;
import com.app.user.dto.UserResponse;
import com.app.user.entity.User;
import com.app.user.entity.UserProfile;
import com.app.user.repository.UserRepository;
import com.app.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Creating user with Account ID: {}", request.getAccountId());

        if (userRepository.existsByAccountId(request.getAccountId())) {
            throw new IllegalArgumentException("User already exists with Account ID: " + request.getAccountId());
        }

        User user = User.builder()
                .accountId(request.getAccountId())
                .username(request.getUsername())
                .email(request.getEmail())
                .status(UserStatus.ACTIVE)
                .build();

        UserProfile profile = UserProfile.builder()
                .user(user)
                .build();
        user.setProfile(profile);

        user = userRepository.save(user);
        log.info("User created successfully: {}", user.getId());

        return mapToResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByAccountId(String accountId) {
        log.debug("Fetching user by accountId: {}", accountId);
        User user = userRepository.findByAccountId(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "accountId", accountId));
        return mapToResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByUserId(String userId) {
        log.debug("Fetching user by userId: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));
        return mapToResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        log.debug("Fetching user by username: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        return mapToResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        log.debug("Fetching all users, page: {}", pageable.getPageNumber());
        return userRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional
    public UserResponse updateUser(String userId, UpdateUserRequest request, String accountId) {
        log.info("Updating userId: {} by accountId: {}", userId, accountId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));

        // Verify ownership
        if (!user.getAccountId().equals(accountId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only update your own information");
        }

        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }

        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }

        user = userRepository.save(user);
        log.info("User updated successfully: {}", userId);

        return mapToResponse(user);
    }

    @Override
    @Transactional
    public void deleteUser(String userId) {
        log.info("Deleting userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));

        user.setStatus(UserStatus.DELETED);
        userRepository.save(user);

        log.info("User deleted (soft delete): {}", userId);
    }

    private UserResponse mapToResponse(User user) {
        UserProfileResponse profileResponse = null;
        if (user.getProfile() != null) {
            UserProfile profile = user.getProfile();
            profileResponse = UserProfileResponse.builder()
                    .id(profile.getId())
                    .firstName(profile.getFirstName())
                    .lastName(profile.getLastName())
                    .bio(profile.getBio())
                    .avatarUrl(profile.getAvatarUrl())
                    .phone(profile.getPhone())
                    .address(profile.getAddress())
                    .city(profile.getCity())
                    .country(profile.getCountry())
                    .dateOfBirth(profile.getDateOfBirth())
                    .gender(profile.getGender())
                    .build();
        }

        return UserResponse.builder()
                .id(user.getId())
                .accountId(user.getAccountId())
                .username(user.getUsername())
                .email(user.getEmail())
                .status(user.getStatus())
                .profile(profileResponse)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}