package com.app.email.controller;


import com.app.common.dto.common.EmailRequest;
import com.app.common.dto.common.EmailResponse;
import com.app.common.dto.response.ApiResponse;
import com.app.email.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/emails")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/send")
    @PreAuthorize("hasRole('SERVICE')")
    public ResponseEntity<ApiResponse<EmailResponse>> sendEmail(
            @Valid @RequestBody EmailRequest request) {

        log.info("Received email request for: {}", request.getTo());

        EmailResponse response = emailService.sendEmail(request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(ApiResponse.success(response, "Email sent successfully"));
        } else {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("EMAIL_SEND_FAILED: "+ response.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("Email service is running"));
    }
}