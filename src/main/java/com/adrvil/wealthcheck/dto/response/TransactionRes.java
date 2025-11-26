package com.adrvil.wealthcheck.dto.response;

import com.adrvil.wealthcheck.enums.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionRes(
        Long id,
        String title,
        String notes,
        BigDecimal amount,
        Long fromWalletId,
        Long toWalletId,
        Long categoryId,
        String fromWalletName,
        String toWalletName,
        String categoryName,
        String categoryIcon,
        TransactionType type,
        Instant transactionDate,
        Instant createdAt,
        Instant updatedAt
) {
}
