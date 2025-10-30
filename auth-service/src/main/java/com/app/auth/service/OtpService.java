package com.app.auth.service;

public interface OtpService {

    String generateOtp(String email);

    boolean validateOtp(String email, String otp);

    void cleanupExpiredOtps();

    boolean canRequestOtp(String email);

    void incrementOtpRequest(String email);
}
