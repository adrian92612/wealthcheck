package com.adrvil.wealthcheck.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record WalletReq(
        @NotNull(message = "Name is required")
        String name,

        @PositiveOrZero(message = "Balance cannot be negative")
        BigDecimal balance
) {
}
