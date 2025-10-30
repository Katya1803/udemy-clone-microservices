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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Creating user with Account ID: {}", request.getAccountId());

        if (userRepository.existsById(request.getAccountId())) {
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
    public UserResponse getUserById(String id) {
        log.debug("Fetching user by ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
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
    public UserResponse updateUser(String id, UpdateUserRequest request) {
        log.info("Updating user: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }

        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }

        user = userRepository.save(user);
        log.info("User updated successfully: {}", id);

        return mapToResponse(user);
    }

    @Override
    @Transactional
    public void deleteUser(String id) {
        log.info("Deleting user: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        user.setStatus(UserStatus.DELETED);
        userRepository.save(user);

        log.info("User deleted (soft delete): {}", id);
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
                .username(user.getUsername())
                .email(user.getEmail())
                .status(user.getStatus())
                .profile(profileResponse)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}