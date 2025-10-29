package com.app.common.client;

public interface IBaseOAuth2TokenProvider {
    /**
     * Get service token for calling another service
     * Token is cached and auto-refreshed before expiration
     *
     * @param audience Target service name (e.g., "user-service")
     * @return Valid service access token
     */
    String getServiceToken(String audience);

    /**
     * Clear cached token (force refresh)
     * Useful when token is rejected by target service
     *
     * @param audience Target service name
     */
    void clearCache(String audience);
}
