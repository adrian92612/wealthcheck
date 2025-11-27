package com.adrvil.wealthcheck.dto.response;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record MoneyBudgetReq(
        @NotNull(message = "Name is required")
        String name,

        @Positive(message = "Amount must be greater than zero")
        BigDecimal amount
) {
}
