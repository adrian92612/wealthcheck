package com.adrvil.wealthcheck.controller;


import com.adrvil.wealthcheck.entity.AccountEntity;
import com.adrvil.wealthcheck.mapper.AccountMapper;
import com.adrvil.wealthcheck.service.CookieService;
import com.adrvil.wealthcheck.service.GoogleAuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
            HttpServletResponse response) throws Exception {

        try {
            String jwtToken = googleAuthService.processGoogleCallback(code);
            cookieService.setJwtCookie(response, jwtToken);


            // Redirect to frontend success page
            response.sendRedirect(googleAuthService.getFrontendUrl() + "/dashboard");

        } catch (Exception e) {
            log.error("Error processing Google callback", e);
            response.sendRedirect(googleAuthService.getFrontendUrl() + "/login?error=auth_failed");
        }
    }


    @GetMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletResponse response) {
        cookieService.clearJwtCookie(response);
        return ResponseEntity.ok(Map.of("message", "Logout successful"));
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> getCurrentUser(Authentication authentication) {
        log.info("Current user: {}", authentication.getPrincipal());
        if (authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            AccountEntity account = accountMapper.findByEmail(authentication.getName());
            if (account == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Account not found"));
            }
            return ResponseEntity.ok(Map.of(
                    "email", account.getEmail(),
                    "name", account.getName(),
                    "authenticated", "true"
            ));
        }
        return ResponseEntity.status(401)
                .body(Map.of("error", "Not authenticated"));
    }

}
