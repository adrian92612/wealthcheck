package com.adrvil.wealthcheck.converter;

import com.adrvil.wealthcheck.dto.request.TransactionReq;
import com.adrvil.wealthcheck.dto.response.TransactionRes;
import com.adrvil.wealthcheck.entity.TransactionEntity;

import java.time.Instant;

public class TransactionDtoMapper {
    // Convert request -> entity
    public static TransactionEntity toEntity(Long userId, TransactionReq req) {
        Instant now = Instant.now();
        return TransactionEntity.builder()
                .userId(userId)
                .fromWalletId(req.fromWalletId())
                .toWalletId(req.toWalletId())
                .categoryId(req.categoryId())
                .title(req.title())
                .notes(req.notes())
                .amount(req.amount())
                .type(req.type())
                .transactionDate(req.transactionDate())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public static TransactionRes toDto(
            TransactionEntity entity,
            String fromWalletName,
            String toWalletName,
            String categoryName,
            String categoryIcon
    ) {
        return new TransactionRes(
                entity.getId(),
                entity.getTitle(),
                entity.getNotes(),
                entity.getAmount(),
                entity.getFromWalletId(),
                entity.getToWalletId(),
                entity.getCategoryId(),
                fromWalletName,
                toWalletName,
                categoryName,
                categoryIcon,
                entity.getType(),
                entity.getTransactionDate(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
