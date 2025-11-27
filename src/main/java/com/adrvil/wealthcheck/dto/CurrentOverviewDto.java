package com.adrvil.wealthcheck.dto;

import java.math.BigDecimal;

public record CurrentOverviewDto(
        BigDecimal totalBalance,
        BigDecimal percentageDifference,
        BigDecimal dailyAverageSpending,
        BigDecimal lastMonthBalance
) {
}
