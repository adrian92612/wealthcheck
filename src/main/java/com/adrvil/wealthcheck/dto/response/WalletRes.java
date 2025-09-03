package com.adrvil.wealthcheck.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record WalletRes(
        Long id,
        String name,
        BigDecimal balance,
        Instant createAt,
        Instant updatedAt
) {
}
