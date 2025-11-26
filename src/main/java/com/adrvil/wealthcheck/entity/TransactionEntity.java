package com.adrvil.wealthcheck.entity;

import com.adrvil.wealthcheck.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class TransactionEntity extends BaseEntity {
    private String title;
    private String notes;
    private BigDecimal amount;
    private Long userId;
    private TransactionType type;
    private Long fromWalletId;
    private Long toWalletId;
    private Long categoryId;
    private Instant transactionDate;
}
