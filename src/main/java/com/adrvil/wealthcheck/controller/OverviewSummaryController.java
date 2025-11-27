package com.adrvil.wealthcheck.controller;

import com.adrvil.wealthcheck.common.api.ApiResponseEntity;
import com.adrvil.wealthcheck.dto.CurrentOverviewDto;
import com.adrvil.wealthcheck.dto.OverviewTopTransactionsDto;
import com.adrvil.wealthcheck.dto.request.MoneyGoalReq;
import com.adrvil.wealthcheck.dto.response.*;
import com.adrvil.wealthcheck.service.OverviewSummaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/overview-summary")
public class OverviewSummaryController {

    private final OverviewSummaryService overviewSummaryService;

    @GetMapping("/current")
    public ApiResponseEntity<CurrentOverviewDto> getOverviewSummary() {
        return ApiResponseEntity.success(HttpStatus.OK, "Overview summary", overviewSummaryService.getOverviewSummary());
    }

    @GetMapping("/top-transactions")
    public ApiResponseEntity<OverviewTopTransactionsDto> getTopTransactions() {
        return ApiResponseEntity.success(HttpStatus.OK, "Top Income/Expense", overviewSummaryService.getTopTransactions());
    }

    @GetMapping("/recent-transactions")
    public ApiResponseEntity<List<TransactionRes>> getRecentTransactionsNew() {
        return ApiResponseEntity.success(HttpStatus.OK, "Recent Transactions", overviewSummaryService.getRecentTransactions());
    }

    @GetMapping("/daily-net-snapshot")
    public ApiResponseEntity<List<DailyNetRes>> getDailyNetSnapshot() {
        return ApiResponseEntity.success(HttpStatus.OK, "Daily Net Snapshot", overviewSummaryService.getDailyNetSnapshot());
    }

    @GetMapping("/top-categories")
    public ApiResponseEntity<TopCategoriesRes> getTopCategories() {
        return ApiResponseEntity.success(HttpStatus.OK, "Top Categories", overviewSummaryService.getTopCategories());
    }

    @GetMapping("/money-goal")
    public ApiResponseEntity<Optional<MoneyGoalRes>> getMoneyGoal() {
        return ApiResponseEntity.success(HttpStatus.OK, "Current money goal", overviewSummaryService.getMoneyGoal());
    }

    @PostMapping("/money-goal")
    public ApiResponseEntity<Optional<MoneyGoalRes>> addMoneyGoal(@Valid @RequestBody MoneyGoalReq req) {
        return ApiResponseEntity.success(HttpStatus.CREATED, "Money goal created", overviewSummaryService.addMoneyGoal(req));
    }

    @GetMapping("/money-budget")
    public ApiResponseEntity<Optional<MoneyBudgetRes>> getMoneyBudget() {
        return ApiResponseEntity.success(HttpStatus.OK, "Current money budget", overviewSummaryService.getMoneyBudget());
    }

    @PostMapping("/money-budget")
    public ApiResponseEntity<Optional<MoneyBudgetRes>> addMoneyGoal(@Valid @RequestBody MoneyBudgetReq req) {
        return ApiResponseEntity.success(HttpStatus.CREATED, "Money budget created", overviewSummaryService.addMoneyBudget(req));
    }
}
