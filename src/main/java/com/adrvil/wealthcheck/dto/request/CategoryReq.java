package com.adrvil.wealthcheck.dto.request;

import com.adrvil.wealthcheck.enums.CategoryType;
import jakarta.validation.constraints.NotNull;

public record CategoryReq(
        @NotNull String name,
        String description,
        @NotNull CategoryType type,
        @NotNull String icon
) {
}
