package com.adrvil.wealthcheck.dto;

import java.util.List;

public record OverviewTopTransactionsDto(
        List<TopTransactionsDto> topIncome,
        List<TopTransactionsDto> topExpense
) {
}
