package com.app.user.service;

import com.app.user.dto.CreateUserRequest;
import com.app.user.dto.UpdateUserRequest;
import com.app.user.dto.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserResponse createUser(CreateUserRequest request);

    UserResponse getUserById(String id);

    UserResponse getUserByUsername(String username);

    Page<UserResponse> getAllUsers(Pageable pageable);

    UserResponse updateUser(String id, UpdateUserRequest request);

    void deleteUser(String id);
}