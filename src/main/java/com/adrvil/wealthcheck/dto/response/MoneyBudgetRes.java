package com.adrvil.wealthcheck.dto.response;

import java.math.BigDecimal;

public record MoneyBudgetRes(
        String name,
        BigDecimal amount,
        BigDecimal spentAmount
) {
}
