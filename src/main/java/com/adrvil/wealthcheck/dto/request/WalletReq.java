package com.adrvil.wealthcheck.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record WalletReq(
//        long userId,
        @NotNull String name,
        @PositiveOrZero BigDecimal balance
) {
}
