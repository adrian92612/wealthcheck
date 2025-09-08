package com.adrvil.wealthcheck.service;

import com.adrvil.wealthcheck.dto.CurrentOverviewDto;
import com.adrvil.wealthcheck.dto.OverviewTopTransactionsDto;
import com.adrvil.wealthcheck.dto.TopTransactionsDto;
import com.adrvil.wealthcheck.mapper.OverviewSummaryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OverviewSummaryService {
    private final OverviewSummaryMapper mapper;

    public CurrentOverviewDto getOverviewSummary() {
        BigDecimal totalBalance = mapper.getTotalBalance();
        BigDecimal incomeThisMonth = mapper.getIncomeThisMonth();
        BigDecimal expenseThisMonth = mapper.getExpenseThisMonth();
        BigDecimal netCashFlow = incomeThisMonth.subtract(expenseThisMonth);

        return new CurrentOverviewDto(
                totalBalance,
                incomeThisMonth,
                expenseThisMonth,
                netCashFlow
        );
    }

    public OverviewTopTransactionsDto getTopCategories() {
        List<TopTransactionsDto> topIncome = mapper.getTopIncomeTransactions();
        List<TopTransactionsDto> topExpense = mapper.getTopExpenseTransactions();
        return new OverviewTopTransactionsDto(topIncome, topExpense);
    }

    public List<TopTransactionsDto> getRecentTransactions() {
        return mapper.getRecentTransactions();
    }
}
