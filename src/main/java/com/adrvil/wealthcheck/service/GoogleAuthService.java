package com.adrvil.wealthcheck.service;


import com.adrvil.wealthcheck.dto.GoogleUserDto;
import com.adrvil.wealthcheck.dto.response.GoogleTokenResponse;
import com.adrvil.wealthcheck.entity.AccountEntity;
import com.adrvil.wealthcheck.common.exception.GoogleAuthException;
import com.adrvil.wealthcheck.mapper.AccountMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class GoogleAuthService {

    private final JwtService jwtService;

    private final RestTemplate restTemplate = new RestTemplate();
    private final AccountMapper accountMapper;
    private final AccountService accountService;

    @Value("${google.oauth2.client-id}")
    private String googleClientId;

    @Value("${google.oauth2.client-secret}")
    private String googleClientSecret;

    @Value("${google.oauth2.redirect-uri}")
    private String googleRedirectUri;

    @Getter
    @Value("${app.frontend.url}")
    private String frontendUrl;

    public String getGoogleAuthUrl() {
        return UriComponentsBuilder
                .fromUriString("https://accounts.google.com/o/oauth2/auth")
                .queryParam("client_id", googleClientId)
                .queryParam("redirect_uri", googleRedirectUri)
                .queryParam("scope", "email profile")
                .queryParam("response_type", "code")
                .queryParam("access_type", "offline")
                .queryParam("prompt", "consent")
                .build()
                .toUriString();
    }

    public String processGoogleCallback(String code) throws GoogleAuthException {
        // Exchange code for access token
        String accessToken = exchangeCodeForToken(code);

        // Get user info from Google
        GoogleUserDto googleUser = getUserInfoFromGoogle(accessToken);
        AccountEntity account = accountMapper.findByEmail(googleUser.email());
        if (account == null) {
            account = accountService.createAccount(googleUser);
        }

        // Generate JWT token with user info
        Map<String, Object> claims = new HashMap<>();
        claims.put("name", account.getName());
        claims.put("avatarUrl", account.getAvatarUrl());

        return jwtService.generateTokenWithClaims(account.getEmail(), claims);
    }

    private String exchangeCodeForToken(String code) throws GoogleAuthException {
        String tokenUrl = "https://oauth2.googleapis.com/token";

        Map<String, String> tokenRequest = new HashMap<>();
        tokenRequest.put("client_id", googleClientId);
        tokenRequest.put("client_secret", googleClientSecret);
        tokenRequest.put("code", code);
        tokenRequest.put("grant_type", "authorization_code");
        tokenRequest.put("redirect_uri", googleRedirectUri);

        try {
            GoogleTokenResponse response = restTemplate.postForObject(tokenUrl, tokenRequest, GoogleTokenResponse.class);
            if (response != null && response.getAccessToken() != null) {
                return response.getAccessToken();
            }
            throw new GoogleAuthException("Failed to get access token from Google");
        } catch (RestClientException e) {
            throw new GoogleAuthException("Error calling Google token endpoint", e);
        }
    }

    private GoogleUserDto getUserInfoFromGoogle(String accessToken) throws GoogleAuthException {
        String userInfoUrl = "https://www.googleapis.com/oauth2/v2/userinfo?access_token=" + accessToken;

        try {
            GoogleUserDto googleUser = restTemplate.getForObject(userInfoUrl, GoogleUserDto.class);
            log.info("Google User: {}", googleUser);

            if (googleUser != null) {
                return googleUser;
            }
            throw new GoogleAuthException("Failed to get user info from Google");
        } catch (RestClientException e) {
            throw new GoogleAuthException("Error calling Google userinfo endpoint", e);
        }
    }
}