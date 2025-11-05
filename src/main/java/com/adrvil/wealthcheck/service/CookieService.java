package com.adrvil.wealthcheck.service;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CookieService {

    @Value("${jwt.cookie.max-age}")
    private int cookieMaxAge;

    @Value("${jwt.cookie.secure}")
    private boolean cookieSecure;

    @Value("${jwt.cookie.name}")
    private String JWT_COOKIE_NAME;

    public void setJwtCookie(HttpServletResponse response, String token) {
        Cookie jwtCookie = new Cookie(JWT_COOKIE_NAME, token);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(cookieSecure);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(cookieMaxAge);
        jwtCookie.setAttribute("SameSite", "Lax");

        response.addCookie(jwtCookie);
        log.debug("JWT cookie set successfully");
    }

    public void clearJwtCookie(HttpServletResponse response) {
        Cookie jwtCookie = new Cookie(JWT_COOKIE_NAME, "");
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(cookieSecure);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);

        response.addCookie(jwtCookie);
        log.debug("JWT cookie cleared successfully");
    }
}