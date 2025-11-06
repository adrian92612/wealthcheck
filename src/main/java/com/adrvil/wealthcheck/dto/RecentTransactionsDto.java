package com.adrvil.wealthcheck.dto;

import com.adrvil.wealthcheck.enums.TransactionType;

import java.math.BigDecimal;

public record RecentTransactionsDto(
        TransactionType type,
        String title,
        BigDecimal amount
) {
}
