package com.adrvil.wealthcheck.dto;

import com.adrvil.wealthcheck.enums.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionForNetDto(
        BigDecimal amount,
        TransactionType type,
        Instant createdAt
) {
}
