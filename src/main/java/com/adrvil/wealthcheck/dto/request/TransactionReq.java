package com.adrvil.wealthcheck.dto.request;


import com.adrvil.wealthcheck.enums.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransactionReq(
        Long fromWalletId,
        Long toWalletId,
        Long categoryId,

        @NotNull(message = "Title is required")
        String title,

        String notes,

        @Positive(message = "Amount must be greater than 0")
        BigDecimal amount,

        @NotNull(message = "Type is required")
        TransactionType type
) {
}
