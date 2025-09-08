package com.adrvil.wealthcheck.dto;

import com.adrvil.wealthcheck.enums.TransactionType;

import java.math.BigDecimal;

public record TopTransactionsDto(
        TransactionType type,
        String name,
        BigDecimal amount
) {
}
