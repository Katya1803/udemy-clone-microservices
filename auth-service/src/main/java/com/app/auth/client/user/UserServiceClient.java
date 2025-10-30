package com.app.auth.client.user;

import com.app.common.dto.response.ApiResponse;
import com.app.common.dto.common.CreateUserRequest;
import com.app.common.dto.common.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(
        name = "user-service",
        configuration = UserServiceFeignConfig.class
)
public interface UserServiceClient {

    @PostMapping("/users")
    ApiResponse<UserResponse> createUser(@RequestBody CreateUserRequest request);
}