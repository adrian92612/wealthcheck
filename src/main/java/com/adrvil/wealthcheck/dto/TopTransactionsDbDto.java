package com.adrvil.wealthcheck.dto;

import com.adrvil.wealthcheck.enums.TransactionType;

import java.math.BigDecimal;

public record TopTransactionsDbDto(
        TransactionType type,
        Long categoryId,
        BigDecimal amount
) {
}
