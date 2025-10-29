package com.app.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {

    private String id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;

    @Builder.Default
    private boolean needsVerification = true;

    private String message;
}