package com.adrvil.wealthcheck.service;

import com.adrvil.wealthcheck.converter.MoneyGoalDtoMapper;
import com.adrvil.wealthcheck.dto.*;
import com.adrvil.wealthcheck.dto.request.MoneyGoalReq;
import com.adrvil.wealthcheck.dto.response.*;
import com.adrvil.wealthcheck.entity.MoneyGoalEntity;
import com.adrvil.wealthcheck.enums.CacheName;
import com.adrvil.wealthcheck.enums.TransactionType;
import com.adrvil.wealthcheck.mapper.OverviewSummaryMapper;
import com.adrvil.wealthcheck.mapper.TransactionMapper;
import com.adrvil.wealthcheck.utils.CacheUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;

    public CurrentOverviewDto getOverviewSummary() {
        Long userId = accountService.getCurrentAccountIdOrThrow();
        String cacheKey = String.valueOf(userId);
        log.debug("Getting overview summary for user: {}", userId);

        Object cachedObj = cacheUtil.get(CacheName.OVERVIEW.getValue(), cacheKey);
        if (cachedObj != null) {
            log.info("Returning cached overview summary for user: {}", userId);
            return objectMapper.convertValue(cachedObj, CurrentOverviewDto.class);
        }

        BigDecimal totalBalance = overviewSummaryMapper.getTotalBalance(userId);
        BigDecimal incomeThisMonth = overviewSummaryMapper.getThisMonthIncomeOrExpense(userId, TransactionType.INCOME);
        BigDecimal expenseThisMonth = overviewSummaryMapper.getThisMonthIncomeOrExpense(userId, TransactionType.EXPENSE);
        BigDecimal netCashFlow = incomeThisMonth.subtract(expenseThisMonth);

        BigDecimal lastMonthBalance = totalBalance.subtract(netCashFlow);

        log.info("lastMonthBalance: {}", lastMonthBalance);

        BigDecimal change = totalBalance.subtract(lastMonthBalance);

        BigDecimal percentageDiff;

        if (lastMonthBalance.compareTo(BigDecimal.ZERO) == 0) {
            int cmp = change.compareTo(BigDecimal.ZERO);
            percentageDiff = cmp > 0 ? BigDecimal.valueOf(100)
                    : cmp < 0 ? BigDecimal.valueOf(-100)
                    : BigDecimal.ZERO;
        } else {
            percentageDiff = change
                    .divide(lastMonthBalance.abs(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }


        int dayOfMonth = LocalDate.now().getDayOfMonth();

        BigDecimal dailyAverageSpending = expenseThisMonth
                .divide(BigDecimal.valueOf(dayOfMonth), 2, RoundingMode.HALF_UP);

        log.info(
                "Overview summary for user {} - Balance: {}, percentageDiff: {}, dailyAverageSpending: {}",
                userId, totalBalance, percentageDiff, dailyAverageSpending
        );

        CurrentOverviewDto result = new CurrentOverviewDto(
                totalBalance,
                percentageDiff,
                dailyAverageSpending,
                lastMonthBalance
        );

        cacheUtil.put(CacheName.OVERVIEW.getValue(), cacheKey, result);

        return result;
    }


    public OverviewTopTransactionsDto getTopTransactions() {
        Long userId = accountService.getCurrentAccountIdOrThrow();
        String cacheKey = String.valueOf(userId);
        log.debug("Getting top transactions for user: {}", userId);

        Object cachedObj = cacheUtil.get(CacheName.TOP_TRANSACTIONS.getValue(), cacheKey);
        if (cachedObj != null) {
            log.info("Returning cached top transactions for user: {}", userId);
            return objectMapper.convertValue(cachedObj, OverviewTopTransactionsDto.class);
        }

//        OverviewTopTransactionsDto cached = cacheUtil.get(CacheName.TOP_TRANSACTIONS.getValue(), cacheKey);
//        if (cached != null) {
//            log.info("Returning cached top transactions for user: {}", userId);
//            return cached;
//        }

        int topTxnCount = 3;

        List<TransactionRes> topIncomes = transactionMapper.getTopTransactions(userId, TransactionType.INCOME, topTxnCount);
        List<TransactionRes> topExpenses = transactionMapper.getTopTransactions(userId, TransactionType.EXPENSE, topTxnCount);

        OverviewTopTransactionsDto result = new OverviewTopTransactionsDto(topIncomes, topExpenses);

        cacheUtil.put(CacheName.TOP_TRANSACTIONS.getValue(), cacheKey, result);

        return result;
    }

    public List<TransactionRes> getRecentTransactions() {
        Long userId = accountService.getCurrentAccountIdOrThrow();
        String cacheKey = String.valueOf(userId);
        log.debug("Getting recent transactions for user: {}", userId);

        List<TransactionRes> cached = cacheUtil.get(CacheName.RECENT_TRANSACTIONS.getValue(), cacheKey);
        if (cached != null) {
            log.info("Returning cached recent transactions for user: {}", userId);
            return cached;
        }

        List<TransactionRes> recentTransactions = transactionMapper.getRecentTransactions(userId, 3);

        log.info("Returning {} recent transactions for user {}", recentTransactions.size(), userId);

        cacheUtil.put(CacheName.RECENT_TRANSACTIONS.getValue(), cacheKey, recentTransactions);

        return recentTransactions;
    }

    public List<DailyNetRes> getDailyNetSnapshot() {
        Long userId = accountService.getCurrentAccountIdOrThrow();
        String cacheKey = String.valueOf(userId);
        log.info("Getting daily snapshot for user: {}", userId);

        // Check cache first
        List<DailyNetRes> cached = cacheUtil.get(CacheName.DAILY_NET.getValue(), cacheKey);
        if (cached != null) {
            log.info("Returning cached daily net snapshot for user: {}", userId);
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
        cacheUtil.put(CacheName.DAILY_NET.getValue(), cacheKey, dailyNetResList);
        log.info("Cached cumulative daily net snapshot for user: {}", userId);

        return dailyNetResList;
    }


    public TopCategoriesRes getTopCategories() {
        Long userId = accountService.getCurrentAccountIdOrThrow();
        String cacheKey = String.valueOf(userId);
        log.debug("Getting top categories for user: {}", userId);

        Object cachedObj = cacheUtil.get(CacheName.TOP_CATEGORIES.getValue(), cacheKey);
        if (cachedObj != null) {
            log.debug("Returning cached top categories for user: {}", userId);
            return objectMapper.convertValue(cachedObj, TopCategoriesRes.class);
        }

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

        TopCategoriesRes result = new TopCategoriesRes(finalTopIncome, finalTopExpense);
        cacheUtil.put(CacheName.TOP_CATEGORIES.getValue(), cacheKey, result);
        return result;
    }

    public Optional<MoneyGoalRes> getMoneyGoal() {
        Long userId = accountService.getCurrentAccountIdOrThrow();
        String cacheKey = String.valueOf(userId);
        String cacheName = CacheName.MONEY_GOAL.getValue();
        log.debug("Getting money goal for user: {}", userId);

//        MoneyGoalRes cached = cacheUtil.get(cacheName, cacheKey);
//        if (cached != null) {
//            log.debug("Returning money goal for user: {}", userId);
//            return Optional.of(cached);
//        }

        Optional<MoneyGoalEntity> result = overviewSummaryMapper.getMoneyGoalByUserId(userId);

        if (result.isEmpty()) {
            return Optional.empty();
        }

        BigDecimal currentBal = overviewSummaryMapper.getTotalBalance(userId);
        MoneyGoalRes finalRes = new MoneyGoalRes(
                result.get().getName(),
                result.get().getAmount(),
                currentBal
        );

//        cacheUtil.put(cacheName, cacheKey, finalRes);

        return Optional.of(finalRes);
    }

    public Optional<MoneyGoalRes> addMoneyGoal(MoneyGoalReq req) {
        Long userId = accountService.getCurrentAccountIdOrThrow();
        log.debug("Creating/updating money goal for user: {}", userId);

        Optional<MoneyGoalEntity> existing = overviewSummaryMapper.getMoneyGoalByUserId(userId);
        MoneyGoalEntity entity = MoneyGoalDtoMapper.toEntity(req, userId);

        if (existing.isEmpty()) {
            overviewSummaryMapper.createMoneyGoal(entity);
        } else {
            int updated = overviewSummaryMapper.updateMoneyGoal(entity);
            if (updated == 0) {
                log.error("Money goal update failed for user: {}", userId);
            }
        }

        Optional<MoneyGoalEntity> moneyGoalEntityOpt = overviewSummaryMapper.getMoneyGoalByUserId(userId);
        if (moneyGoalEntityOpt.isEmpty()) {
            return Optional.empty();
        }

        BigDecimal currentBal = overviewSummaryMapper.getTotalBalance(userId);
        MoneyGoalEntity moneyGoalEntity = moneyGoalEntityOpt.get();

        return Optional.of(MoneyGoalDtoMapper.toDto(moneyGoalEntity, currentBal));
    }


//    HELPER METHODS

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