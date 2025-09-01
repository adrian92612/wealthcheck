package com.adrvil.wealthcheck.dto.response;

import com.adrvil.wealthcheck.enums.CategoryType;

import java.time.Instant;

public record CategoryRes(
        Long id,
        String name,
        String description,
        CategoryType type,
        String icon,
        boolean isActive,
        Instant createAt,
        Instant updatedAt
) {
}
