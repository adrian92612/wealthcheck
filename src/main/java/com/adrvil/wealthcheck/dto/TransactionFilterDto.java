package com.adrvil.wealthcheck.dto;

import com.adrvil.wealthcheck.enums.TransactionType;

import java.time.LocalDate;

public record TransactionFilterDto(
        Integer page,
        Integer size,
        TransactionType type,
        Long walletId,
        Long categoryId,
        LocalDate fromDate,
        LocalDate toDate,
        String search
) {
}
