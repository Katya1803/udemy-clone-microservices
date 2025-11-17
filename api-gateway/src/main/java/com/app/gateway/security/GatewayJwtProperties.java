package com.app.gateway.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class GatewayJwtProperties {

    private String publicKey;

    private String issuer = "auth-service";

    private Long accessTokenExpiration = 900000L;

    private Long refreshTokenExpiration = 604800000L;

    private Long serviceTokenExpiration = 300000L;

    public boolean isRsaMode() {
        return publicKey != null && !publicKey.isBlank();
    }

}