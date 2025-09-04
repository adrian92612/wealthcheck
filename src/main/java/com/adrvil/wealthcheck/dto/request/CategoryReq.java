package com.adrvil.wealthcheck.dto.request;

import com.adrvil.wealthcheck.enums.CategoryType;
import jakarta.validation.constraints.NotNull;

public record CategoryReq(
        @NotNull(message = "Name is required")
        String name,

        String description,

        @NotNull(message = "Type is required")
        CategoryType type,

        @NotNull(message = "Icon is required")
        String icon
) {
}
