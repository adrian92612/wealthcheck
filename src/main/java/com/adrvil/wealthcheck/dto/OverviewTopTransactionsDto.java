package com.adrvil.wealthcheck.dto;

import com.adrvil.wealthcheck.dto.response.TransactionRes;

import java.util.List;

public record OverviewTopTransactionsDto(
        List<TransactionRes> topIncome,
        List<TransactionRes> topExpense
) {
}
