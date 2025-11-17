package com.app.common.security;

import com.app.common.constant.SecurityConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Data
@Component
@ConfigurationProperties(prefix = SecurityConstants.JWT)
public class JwtProperties {

    private String publicKey;

    private String privateKey;

    private String issuer = SecurityConstants.ISSUER;

    private Long accessTokenExpiration = SecurityConstants.ACCESS_TOKEN_EXP;

    private Long refreshTokenExpiration = SecurityConstants.REFRESH_TOKEN_EXP;

    private Long serviceTokenExpiration = SecurityConstants.SERVICE_TOKEN_EXP;

    public boolean isRsaMode() {
        return publicKey != null && !publicKey.isBlank();
    }

}