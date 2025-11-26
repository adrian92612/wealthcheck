package com.adrvil.wealthcheck.converter;

import com.adrvil.wealthcheck.dto.request.MoneyGoalReq;
import com.adrvil.wealthcheck.dto.response.MoneyGoalRes;
import com.adrvil.wealthcheck.entity.MoneyGoalEntity;

import java.math.BigDecimal;
import java.time.Instant;

public class MoneyGoalDtoMapper {
    public static MoneyGoalEntity toEntity(MoneyGoalReq req, Long userId) {
        return MoneyGoalEntity.builder()
                .name(req.name())
                .amount(req.amount())
                .userId(userId)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .softDeleted(false)
                .build();
    }

    public static MoneyGoalRes toDto(MoneyGoalEntity entity, BigDecimal currentBalance) {
        return new MoneyGoalRes(
                entity.getName(),
                entity.getAmount(),
                currentBalance
        );
    }
}
