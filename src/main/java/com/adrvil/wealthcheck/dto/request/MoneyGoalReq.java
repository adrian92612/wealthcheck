package com.adrvil.wealthcheck.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record MoneyGoalReq(
        @NotNull(message = "Name is required")
        String name,

        @Positive(message = "Amount must be greater than zero")
        BigDecimal amount
) {
}
