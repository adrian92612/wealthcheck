package com.adrvil.wealthcheck.controller;

import com.adrvil.wealthcheck.common.api.ApiResponseEntity;
import com.adrvil.wealthcheck.dto.CurrentOverviewDto;
import com.adrvil.wealthcheck.dto.OverviewTopTransactionsDto;
import com.adrvil.wealthcheck.dto.RecentTransactionsDto;
import com.adrvil.wealthcheck.service.OverviewSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
        return ApiResponseEntity.success(HttpStatus.OK, "Top Income/Expense", overviewSummaryService.getTopCategories());
    }

    @GetMapping("/recent-transactions")
    public ApiResponseEntity<List<RecentTransactionsDto>> getRecentTransactions() {
        return ApiResponseEntity.success(HttpStatus.OK, "Recent Transactions", overviewSummaryService.getRecentTransactions());
    }
}
