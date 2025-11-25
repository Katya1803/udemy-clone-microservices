package com.app.course.client;

import com.app.common.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "user-service",
        configuration = UserServiceFeignConfig.class
)
public interface UserServiceClient {

    @GetMapping("/users/account/{accountId}")
    ApiResponse<UserDto> getUserByAccountId(@PathVariable("accountId") String accountId);
}