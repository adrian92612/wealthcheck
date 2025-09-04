package com.adrvil.wealthcheck.controller;

import com.adrvil.wealthcheck.common.api.ApiResponseEntity;
import com.adrvil.wealthcheck.entity.AccountEntity;
import com.adrvil.wealthcheck.common.exception.GoogleAuthException;
import com.adrvil.wealthcheck.mapper.AccountMapper;
import com.adrvil.wealthcheck.service.CookieService;
import com.adrvil.wealthcheck.service.GoogleAuthService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/auth")
public class GoogleAuthController {

    private final GoogleAuthService googleAuthService;
    private final CookieService cookieService;
    private final AccountMapper accountMapper;

    @GetMapping("/google")
    public void login(HttpServletResponse response) throws IOException {
        String googleAuthUrl = googleAuthService.getGoogleAuthUrl();
        response.sendRedirect(googleAuthUrl);
    }

    @GetMapping("/callback")
    public void handleGoogleCallback(
            @RequestParam String code,
            HttpServletResponse response) throws IOException {

        try {
            String jwtToken = googleAuthService.processGoogleCallback(code);
            cookieService.setJwtCookie(response, jwtToken);

            response.sendRedirect(googleAuthService.getFrontendUrl() + "/dashboard");

        } catch (GoogleAuthException | IOException e) {
            log.error("Google OAuth failed", e);
            response.sendRedirect(googleAuthService.getFrontendUrl() + "/login?error=auth_failed");
        }
    }


    @GetMapping("/logout")
    public void logout(HttpServletResponse response) throws IOException {
        cookieService.clearJwtCookie(response);
        response.sendRedirect(googleAuthService.getFrontendUrl() + "/login");
    }

    @GetMapping("/me")
    public ApiResponseEntity<Map<String, String>> getCurrentUser(Authentication authentication) {
        log.info("Current user: {}", authentication.getPrincipal());
        if (authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            AccountEntity account = accountMapper.findByEmail(authentication.getName());
            if (account == null) {
                return ApiResponseEntity.error(HttpStatus.NOT_FOUND, "Account not found", null);
            }
            return ApiResponseEntity.success(HttpStatus.OK, "Authenticated", Map.of(
                    "email", account.getEmail(),
                    "name", account.getName()
            ));
        }
        return ApiResponseEntity.error(HttpStatus.UNAUTHORIZED, "Not Authenticated", null);
    }

}
