package com.app.user.dto;

import com.app.user.constant.Gender;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserProfileResponse {

    private String id;
    private String firstName;
    private String lastName;
    private String bio;
    private String avatarUrl;
    private String phone;
    private String address;
    private String city;
    private String country;
    private LocalDate dateOfBirth;
    private Gender gender;
}