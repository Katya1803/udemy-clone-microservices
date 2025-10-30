package com.app.auth.service.impl;

import com.app.auth.entity.OtpCode;
import com.app.auth.repository.OtpCodeRepository;
import com.app.auth.service.OtpService;
import com.app.common.constant.RedisConstants;
import com.app.common.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    final OtpCodeRepository otpCodeRepository;
    final RedisTemplate<String, String> redisTemplate;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final long otpExpiration = 300;
    private static final int otpLength = 6;

    @Override
    @Transactional
    public String generateOtp(String email) {
        log.info("Generating OTP for email: {}", email);

        String code = generateRandomCode();
        Instant expriresAt = Instant.now().plusSeconds(otpExpiration);

        OtpCode otpCode = OtpCode.builder()
                .email(email)
                .code(code)
                .expiresAt(expriresAt)
                .build();
        otpCodeRepository.save(otpCode);

        String redisKey = RedisConstants.OTP_REDIS_PREFIX + email;
        redisTemplate.opsForValue().set(
                redisKey,
                code,
                Duration.ofSeconds(otpExpiration)
        );

        return code;
    }

    @Override
    public boolean validateOtp(String email, String otp) {

        String redisKey = RedisConstants.OTP_REDIS_PREFIX + email;
        String cachedOtp = redisTemplate.opsForValue().get(redisKey);

        if (cachedOtp != null && cachedOtp.equals(otp)) {
            Optional<OtpCode> otpCode = otpCodeRepository.findByEmailAndCodeAndVerifiedFalse(email, otp);
            if (otpCode.isPresent()) {
                OtpCode otpValue = otpCode.get();
                if (otpValue.isValid()) {
                    otpValue.setVerified(true);
                    otpCodeRepository.save(otpValue);

                    redisTemplate.delete(redisKey);

                    log.info("OTP validated successfully for email: {}", email);
                    return true;
                }
            }
        }

        Optional<OtpCode> otpOpt = otpCodeRepository.findByEmailAndCodeAndVerifiedFalse(email, otp);
        if (otpOpt.isPresent()) {
            OtpCode otpCode = otpOpt.get();
            if (otpCode.isValid()) {
                otpCode.setVerified(true);
                otpCodeRepository.save(otpCode);

                // Remove from Redis
                redisTemplate.delete(redisKey);

                log.info("OTP validated successfully from database for email: {}", email);
                return true;
            } else {
                log.warn("OTP expired for email: {}", email);
                throw new UnauthorizedException("OTP has expired");
            }
        }

        log.warn("Invalid OTP for email: {}", email);
        return false;
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    @Override
    public void cleanupExpiredOtps() {
        log.info("Running cleanup task for expired OTPs");
        otpCodeRepository.deleteExpiredOtps(Instant.now());
    }

    @Override
    public boolean canRequestOtp(String email) {
        String rateLimitKey = RedisConstants.OTP_RATE_LIMIT_KEY + email;
        String attempts = redisTemplate.opsForValue().get(rateLimitKey);

        if (attempts != null) {
            int count = Integer.parseInt(attempts);
            if (count >= 3) {
                log.warn("OTP rate limit exceeded for email: {}", email);
                return false;
            }
        }

        return true;
    }

    @Override
    public void incrementOtpRequest(String email) {
        String rateLimitKey = RedisConstants.OTP_RATE_LIMIT_KEY + email;
        Long count = redisTemplate.opsForValue().increment(rateLimitKey);

        if (count != null && count == 1) {
            redisTemplate.expire(rateLimitKey, 15, TimeUnit.MINUTES);
        }
    }

    private String generateRandomCode() {
        int min = (int) Math.pow(10, otpLength - 1);
        int max = (int) Math.pow(10, otpLength) - 1;
        int code = RANDOM.nextInt(max - min + 1) + min;
        return String.valueOf(code);
    }
}
