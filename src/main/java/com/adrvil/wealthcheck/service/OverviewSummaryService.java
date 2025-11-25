package com.adrvil.wealthcheck.service;

import com.adrvil.wealthcheck.dto.*;
import com.adrvil.wealthcheck.dto.response.CategoryPieRes;
import com.adrvil.wealthcheck.dto.response.DailyNetRes;
import com.adrvil.wealthcheck.dto.response.TopCategoriesRes;
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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
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
        BigDecimal lastMonthBalance = totalBalance.subtract(netCashFlow);
        BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

        BigDecimal percentageDiff;

        if (lastMonthBalance.compareTo(BigDecimal.ZERO) == 0) {

            int cmp = netCashFlow.compareTo(BigDecimal.ZERO);

            if (cmp > 0) {
                percentageDiff = ONE_HUNDRED; // +100%
            } else if (cmp == 0) {
                percentageDiff = BigDecimal.ZERO; // 0%
            } else {
                percentageDiff = ONE_HUNDRED.negate(); // -100%
            }

        } else {
            percentageDiff = netCashFlow
                    .subtract(lastMonthBalance)
                    .divide(lastMonthBalance, 4, RoundingMode.HALF_UP)
                    .multiply(ONE_HUNDRED);
        }

        int dayOfMonth = LocalDate.now().getDayOfMonth();
        BigDecimal dailyAverageSpending = expenseThisMonth
                .divide(BigDecimal.valueOf(dayOfMonth), 2, RoundingMode.HALF_UP);

        log.info("Overview summary for user {} - Balance: {}, Income: {}, Expense: {}, Net: {}",
                userId, totalBalance, incomeThisMonth, expenseThisMonth, netCashFlow);

        CurrentOverviewDto result = new CurrentOverviewDto(
                totalBalance,
                percentageDiff,
                dailyAverageSpending
        );

        cacheUtil.put(CacheName.OVERVIEW.getValue(), String.valueOf(userId), result);

        return result;
    }

    public OverviewTopTransactionsDto getTopTransactions() {
        Long userId = accountService.getCurrentAccountIdOrThrow();
        log.debug("Getting top transactions for user: {}", userId);

        int topTxnCount = 3;

        List<TransactionRes> topIncomes = transactionMapper.getTopTransactions(userId, TransactionType.INCOME, topTxnCount);
        List<TransactionRes> topExpenses = transactionMapper.getTopTransactions(userId, TransactionType.EXPENSE, topTxnCount);

        OverviewTopTransactionsDto result = new OverviewTopTransactionsDto(topIncomes, topExpenses);

        cacheUtil.put(CacheName.TOP_TRANSACTIONS.getValue(), String.valueOf(userId), result);

        return result;
    }

    public List<TransactionRes> getRecentTransactions() {
        Long userId = accountService.getCurrentAccountIdOrThrow();
        log.debug("Getting recent transactions for user: {}", userId);

        List<TransactionRes> recentTransactions = transactionMapper.getRecentTransactions(userId, 3);

        log.info("Returning {} recent transactions for user {}", recentTransactions.size(), userId);

        cacheUtil.put(CacheName.RECENT_TRANSACTIONS.getValue(), String.valueOf(userId), recentTransactions);

        return recentTransactions;
    }

    public List<DailyNetRes> getDailyNetSnapshot() {
        Long userId = accountService.getCurrentAccountIdOrThrow();
        log.debug("Getting daily snapshot for user: {}", userId);

        // Check cache first
        List<DailyNetRes> cached = cacheUtil.get(CacheName.DAILY_NET.getValue(), String.valueOf(userId));
        if (cached != null) {
            log.debug("Returning cached daily net snapshot for user: {}", userId);
            return cached;
        }

        LocalDate startDate = LocalDate.now().withDayOfMonth(1);
        LocalDate endDate = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()).plusDays(1);
        log.debug("Snapshot period: {} to {}", startDate, endDate.minusDays(1));

        // Fetch transactions
        List<TransactionForNetDto> transactionForNetDtoList = overviewSummaryMapper.getTransactionForNetList(userId, startDate, endDate);
        log.debug("Fetched {} transactions for daily net calculation", transactionForNetDtoList.size());

        // 1️⃣ Aggregate net per day
        Map<LocalDate, BigDecimal> dailyNetMap = new HashMap<>();
        for (TransactionForNetDto tx : transactionForNetDtoList) {
            LocalDate date = tx.createdAt().atZone(ZoneId.of("Asia/Manila")).toLocalDate();
            BigDecimal amount = tx.type() == TransactionType.INCOME ? tx.amount() : tx.amount().negate();
            dailyNetMap.put(date, dailyNetMap.getOrDefault(date, BigDecimal.ZERO).add(amount));
            log.trace("Processed transaction for {}: {} (Type: {})", date, amount, tx.type());
        }

        // 2️⃣ Build daily list with cumulative total
        List<DailyNetRes> dailyNetResList = new ArrayList<>();
        BigDecimal runningTotal = BigDecimal.ZERO;
        LocalDate today = LocalDate.now();
        LocalDate current = startDate;

        while (current.isBefore(endDate)) {
            if (current.isAfter(today)) {
                dailyNetResList.add(new DailyNetRes(current, null));
            } else {
                BigDecimal dailyNet = dailyNetMap.getOrDefault(current, BigDecimal.ZERO);
                runningTotal = runningTotal.add(dailyNet);
                dailyNetResList.add(new DailyNetRes(current, runningTotal));
            }

            current = current.plusDays(1);
        }

        // Cache result
        cacheUtil.put(CacheName.DAILY_NET.getValue(), String.valueOf(userId), dailyNetResList);
        log.debug("Cached cumulative daily net snapshot for user: {}", userId);

        return dailyNetResList;
    }


    public TopCategoriesRes getTopCategories() {
        Long userId = accountService.getCurrentAccountIdOrThrow();
//        String cacheKey = String.valueOf(userId);
        log.debug("Getting top categories for user: {}", userId);

//        TopCategoriesRes cached = cacheUtil.get(CacheName.TOP_CATEGORIES.getValue(), cacheKey);
//        if (cached != null) {
//            log.debug("Returning cached top categories for user: {}", userId);
//            return cached;
//        }

//        Object cachedObj = cacheUtil.get(CacheName.TOP_CATEGORIES.getValue(), cacheKey);
//        if (cachedObj != null) {
//            log.debug("Returning cached top categories for user: {}", userId);
//            return new ObjectMapper().convertValue(cachedObj, TopCategoriesRes.class);
//        }

        LocalDate startDate = LocalDate.now().withDayOfMonth(1);
        LocalDate endDate = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()).plusDays(1);
        log.debug("Top categories period: {} to {}", startDate, endDate.minusDays(1));

        List<CategoryPieRes> topIncomeCategories = overviewSummaryMapper.getTopCategories(userId, TransactionType.INCOME, startDate, endDate);
        List<CategoryPieRes> topExpenseCategories = overviewSummaryMapper.getTopCategories(userId, TransactionType.EXPENSE, startDate, endDate);

        List<CategoryPieRes> finalTopIncome;
        List<CategoryPieRes> finalTopExpense;

        if (topIncomeCategories.size() > 3) {
            finalTopIncome = reduceTopCategories(topIncomeCategories);
        } else {
            finalTopIncome = topIncomeCategories;
        }

        if (topExpenseCategories.size() > 3) {
            finalTopExpense = reduceTopCategories(topExpenseCategories);
        } else {
            finalTopExpense = topExpenseCategories;
        }

//        TopCategoriesRes result =
//        cacheUtil.put(CacheName.TOP_CATEGORIES.getValue(), cacheKey, result);
        return new TopCategoriesRes(finalTopIncome, finalTopExpense);
    }

    private List<CategoryPieRes> reduceTopCategories(List<CategoryPieRes> categories) {
        List<CategoryPieRes> top3 = new ArrayList<>(categories.subList(0, 3));
        BigDecimal othersAmount = categories.subList(3, categories.size())
                .stream()
                .map(CategoryPieRes::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        top3.add(new CategoryPieRes("Other Categories", othersAmount));
        return top3;
    }

}