package com.adrvil.wealthcheck.converter;

import com.adrvil.wealthcheck.dto.response.MoneyBudgetReq;
import com.adrvil.wealthcheck.dto.response.MoneyBudgetRes;
import com.adrvil.wealthcheck.entity.MoneyBudgetEntity;

import java.math.BigDecimal;
import java.time.Instant;

public class MoneyBudgetDtoMapper {
    public static MoneyBudgetEntity toEntity(MoneyBudgetReq req, Long userId) {
        return MoneyBudgetEntity.builder()
                .name(req.name())
                .amount(req.amount())
                .userId(userId)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .softDeleted(false)
                .build();
    }

    public static MoneyBudgetRes toDto(MoneyBudgetEntity entity, BigDecimal spentAmount) {
        return new MoneyBudgetRes(
                entity.getName(),
                entity.getAmount(),
                spentAmount
        );
    }
}
