package com.app.auth.service;

import com.app.auth.entity.Account;


public interface JwtTokenGenerator {

    String generateAccessToken(Account account);

    String generateServiceToken(String clientId, String audience, String scope);

    long getAccessTokenExpirationSeconds();

    long getServiceTokenExpirationSeconds();

}
