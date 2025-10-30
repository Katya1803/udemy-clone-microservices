package com.app.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Data
@Configuration
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitProperties {

    private Integer defaultLimit = 100;

    private Integer defaultDuration = 60;

    private Integer authLoginLimit = 5;

    private Integer authLoginDuration = 900;
}