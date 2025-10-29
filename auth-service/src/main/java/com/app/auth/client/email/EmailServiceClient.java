package com.app.auth.client.email;

import com.app.common.dto.response.ApiResponse;
import com.app.common.dto.common.EmailRequest;
import com.app.common.dto.common.EmailResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * For service-to-service email sending
 */
@FeignClient(
        name = "email-service",
        configuration = EmailServiceFeignConfig.class
)
public interface EmailServiceClient {

    @PostMapping("/emails/send")
    ApiResponse<EmailResponse> sendEmail(@RequestBody EmailRequest request);
}