package com.adrvil.wealthcheck.converter;

import com.adrvil.wealthcheck.dto.request.WalletReq;
import com.adrvil.wealthcheck.dto.response.WalletRes;
import com.adrvil.wealthcheck.entity.WalletEntity;

import java.time.Instant;

public class WalletDtoMapper {
    public static WalletEntity toEntity(Long userId, WalletReq walletReq) {
        return WalletEntity.builder()
                .userId(userId)
                .name(walletReq.name())
                .balance(walletReq.balance())
                .softDeleted(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public static WalletRes toDto(WalletEntity walletEntity) {
        return new WalletRes(
                walletEntity.getId(),
                walletEntity.getName(),
                walletEntity.getBalance(),
                walletEntity.getCreatedAt(),
                walletEntity.getUpdatedAt()
        );
    }
}
