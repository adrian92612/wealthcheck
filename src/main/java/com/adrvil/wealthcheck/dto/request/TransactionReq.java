package com.adrvil.wealthcheck.dto.request;


import com.adrvil.wealthcheck.enums.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record TransactionReq(
        Long fromWalletId,
        Long toWalletId,
        Long categoryId,
        @NotNull String title,
        @NotNull String notes,
        @PositiveOrZero BigDecimal amount,
        @NotNull TransactionType type
) {
}
