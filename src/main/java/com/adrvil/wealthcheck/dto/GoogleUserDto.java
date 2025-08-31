package com.adrvil.wealthcheck.dto;

public record GoogleUserDto(
        String id,
        String name,
        String email,
        String picture,
        boolean verifiedEmail
) {
}
