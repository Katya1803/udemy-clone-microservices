package com.app.user.dto;

import com.app.user.constant.UserStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {

    private String id;
    private String accountId;
    private String username;
    private String email;
    private UserStatus status;
    private UserProfileResponse profile;
    private Instant createdAt;
    private Instant updatedAt;
}