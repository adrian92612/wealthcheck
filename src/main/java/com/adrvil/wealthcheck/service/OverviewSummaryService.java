package com.adrvil.wealthcheck.service;

import com.adrvil.wealthcheck.dto.*;
import com.adrvil.wealthcheck.enums.TransactionType;
import com.adrvil.wealthcheck.mapper.OverviewSummaryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class OverviewSummaryService {
    private final OverviewSummaryMapper overviewSummaryMapper;
    private final AccountService accountService;

    public CurrentOverviewDto getOverviewSummary() {
        Long userId = accountService.getCurrentAccountIdOrThrow();
        log.debug("Getting overview summary for user: {}", userId);

        BigDecimal totalBalance = overviewSummaryMapper.getTotalBalance(userId);
        BigDecimal incomeThisMonth = overviewSummaryMapper.getThisMonthIncomeOrExpense(userId, TransactionType.INCOME);
        BigDecimal expenseThisMonth = overviewSummaryMapper.getThisMonthIncomeOrExpense(userId, TransactionType.EXPENSE);
        BigDecimal netCashFlow = incomeThisMonth.subtract(expenseThisMonth);

        log.info("Overview summary for user {} - Balance: {}, Income: {}, Expense: {}, Net: {}",
                userId, totalBalance, incomeThisMonth, expenseThisMonth, netCashFlow);

        return new CurrentOverviewDto(
                totalBalance,
                incomeThisMonth,
                expenseThisMonth,
                netCashFlow
        );
    }

    public OverviewTopTransactionsDto getTopCategories() {
        Long userId = accountService.getCurrentAccountIdOrThrow();
        log.debug("Getting top categories for user: {}", userId);

        List<TopTransactionsDbDto> topIncome = overviewSummaryMapper.getTopTransactions(userId, TransactionType.INCOME, 3);
        List<TopTransactionsDbDto> topExpense = overviewSummaryMapper.getTopTransactions(userId, TransactionType.EXPENSE, 3);

        log.debug("Found {} top income and {} top expense transactions for user {}",
                topIncome.size(), topExpense.size(), userId);

        Set<Long> allCategoryIds = Stream.concat(
                topIncome.stream().map(TopTransactionsDbDto::categoryId),
                topExpense.stream().map(TopTransactionsDbDto::categoryId)
        ).filter(Objects::nonNull).collect(Collectors.toSet());

        log.debug("Looking up {} category names for user {}", allCategoryIds.size(), userId);

        Map<Long, String> categoryNames;
        if (allCategoryIds.isEmpty()) {
            log.debug("No category IDs to look up, using empty map");
            categoryNames = Collections.emptyMap();
        } else {
            List<CategoryNameProjection> categories = overviewSummaryMapper.findCategoryNamesByIds(userId, allCategoryIds);
            categoryNames = categories.stream()
                    .collect(Collectors.toMap(
                            CategoryNameProjection::id,
                            CategoryNameProjection::name
                    ));
            log.debug("Successfully mapped {} category names", categoryNames.size());
        }

        log.debug("Successfully mapped {} category names", categoryNames.size());

        List<TopTransactionsDto> topIncomeWithNames = topIncome.stream()
                .map(dto -> new TopTransactionsDto(
                        dto.type(),
                        categoryNames.get(dto.categoryId()),
                        dto.amount()
                ))
                .collect(Collectors.toList());

        List<TopTransactionsDto> topExpenseWithNames = topExpense.stream()
                .map(dto -> new TopTransactionsDto(
                        dto.type(),
                        categoryNames.get(dto.categoryId()),
                        dto.amount()
                ))
                .collect(Collectors.toList());

        log.info("Returning top categories for user {} - Income: {}, Expense: {}",
                userId, topIncomeWithNames.size(), topExpenseWithNames.size());

        return new OverviewTopTransactionsDto(topIncomeWithNames, topExpenseWithNames);
    }

    public List<RecentTransactionsDto> getRecentTransactions() {
        Long userId = accountService.getCurrentAccountIdOrThrow();
        log.debug("Getting recent transactions for user: {}", userId);

        List<RecentTransactionsDto> recentTransactions = overviewSummaryMapper.getRecentTransactions(userId);

        log.info("Returning {} recent transactions for user {}", recentTransactions.size(), userId);
        return recentTransactions;
    }
}