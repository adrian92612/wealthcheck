package com.adrvil.wealthcheck.dto.response;

import com.adrvil.wealthcheck.dto.TransactionFilterDto;

import java.util.List;

public record TransactionFilterRes(
        List<TransactionRes> transactions,
        TransactionFilterDto filters,
        long totalItems,   // total matching items in DB
        int currentPage,   // 1-based index
        int pageSize,      // requested page size
        int totalPages     // derived = ceil(totalItems / pageSize)
) {
    public static TransactionFilterRes of(List<TransactionRes> transactions,
                                          TransactionFilterDto filters,
                                          long totalItems) {
        int page = filters.page() != null ? filters.page() : 1;
        int size = filters.size() != null ? filters.size() : transactions.size();

        int totalPages = (size > 0)
                ? (int) Math.ceil((double) totalItems / size)
                : 1;

        return new TransactionFilterRes(
                transactions,
                filters,
                totalItems,
                page,
                size,
                totalPages
        );
    }
}
