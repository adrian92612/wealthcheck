package com.adrvil.wealthcheck.service;

import com.adrvil.wealthcheck.dto.*;
import com.adrvil.wealthcheck.dto.response.TransactionRes;
import com.adrvil.wealthcheck.enums.CacheName;
import com.adrvil.wealthcheck.enums.TransactionType;
import com.adrvil.wealthcheck.mapper.OverviewSummaryMapper;
import com.adrvil.wealthcheck.mapper.TransactionMapper;
import com.adrvil.wealthcheck.utils.CacheUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OverviewSummaryService {
    private final OverviewSummaryMapper overviewSummaryMapper;
    private final TransactionMapper transactionMapper;
    private final AccountService accountService;
    private final CacheUtil cacheUtil;

    public CurrentOverviewDto getOverviewSummary() {
        Long userId = accountService.getCurrentAccountIdOrThrow();
        log.debug("Getting overview summary for user: {}", userId);

        BigDecimal totalBalance = overviewSummaryMapper.getTotalBalance(userId);
        BigDecimal incomeThisMonth = overviewSummaryMapper.getThisMonthIncomeOrExpense(userId, TransactionType.INCOME);
        BigDecimal expenseThisMonth = overviewSummaryMapper.getThisMonthIncomeOrExpense(userId, TransactionType.EXPENSE);
        BigDecimal netCashFlow = incomeThisMonth.subtract(expenseThisMonth);

        log.info("Overview summary for user {} - Balance: {}, Income: {}, Expense: {}, Net: {}",
                userId, totalBalance, incomeThisMonth, expenseThisMonth, netCashFlow);

        CurrentOverviewDto result = new CurrentOverviewDto(
                totalBalance,
                incomeThisMonth,
                expenseThisMonth,
                netCashFlow
        );

        cacheUtil.put(CacheName.OVERVIEW.getValue(), String.valueOf(userId), result);

        return result;
    }

    public OverviewTopTransactionsDto getTopCategories() {
        Long userId = accountService.getCurrentAccountIdOrThrow();
        log.debug("Getting top categories for user: {}", userId);

        int topTxnCount = 5;

        List<TransactionRes> topIncomes = transactionMapper.getTopTransactions(userId, TransactionType.INCOME, topTxnCount);
        List<TransactionRes> topExpenses = transactionMapper.getTopTransactions(userId, TransactionType.EXPENSE, topTxnCount);

        OverviewTopTransactionsDto result = new OverviewTopTransactionsDto(topIncomes, topExpenses);

        cacheUtil.put(CacheName.TOP_TRANSACTIONS.getValue(), String.valueOf(userId), result);

        return result;
    }

    public List<TransactionRes> getRecentTransactions() {
        Long userId = accountService.getCurrentAccountIdOrThrow();
        log.debug("Getting recent transactions for user: {}", userId);

        List<TransactionRes> recentTransactions = transactionMapper.getRecentTransactions(userId);

        log.info("Returning {} recent transactions for user {}", recentTransactions.size(), userId);

        cacheUtil.put(CacheName.RECENT_TRANSACTIONS.getValue(), String.valueOf(userId), recentTransactions);

        return recentTransactions;
    }
}