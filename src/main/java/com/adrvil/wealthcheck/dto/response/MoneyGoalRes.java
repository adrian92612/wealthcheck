package com.adrvil.wealthcheck.dto.response;

import java.math.BigDecimal;

public record MoneyGoalRes(
        String name,
        BigDecimal amount,
        BigDecimal currentBalance
) {
}
