package com.mysawit.plantation.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.Map;

/**
 * Internal HTTP client for fetching user details from the Identity Service.
 * Uses the dedicated /api/internal/users/{id} endpoint with X-Internal-Api-Key header.
 * Failures are non-fatal: returns null so callers fall back gracefully.
 */
@Component
public class IdentityServiceClient {

    private static final Logger log = LoggerFactory.getLogger(IdentityServiceClient.class);

    private final RestTemplate restTemplate;
    private final String identityServiceUrl;
    private final String internalApiKey;

    public IdentityServiceClient(
            @Value("${identity.service.url:http://localhost:8081}") String identityServiceUrl,
            @Value("${identity.service.internal-api-key:}") String internalApiKey
    ) {
        this.restTemplate = new RestTemplate();
        this.identityServiceUrl = identityServiceUrl;
        this.internalApiKey = internalApiKey;
    }

    /**
     * Fetch the display name of a user by their UUID from the Identity Service internal API.
     * Returns null if the user is not found or the call fails.
     */
    public String getUserName(String userId) {
        if (userId == null || userId.isBlank()) {
            return null;
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            if (internalApiKey != null && !internalApiKey.isBlank()) {
                headers.set("X-Internal-Api-Key", internalApiKey);
            }
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            String url = identityServiceUrl + "/api/internal/users/" + userId;

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object name = response.getBody().get("name");
                if (name instanceof String s && !s.isBlank()) {
                    return s.trim();
                }
            }
        } catch (Exception e) {
            log.warn("Could not fetch user name for userId={}: {}", userId, e.getMessage());
        }
        return null;
    }
}
