package com.adrvil.wealthcheck.dto;

import java.math.BigDecimal;

public record CurrentOverviewDto(
        BigDecimal totalBalance,
        BigDecimal incomeThisMonth,
        BigDecimal expenseThisMonth,
        BigDecimal netCashFlow
) {
}
