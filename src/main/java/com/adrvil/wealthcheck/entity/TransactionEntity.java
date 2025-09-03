package com.adrvil.wealthcheck.entity;

import com.adrvil.wealthcheck.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class TransactionEntity extends BaseEntity {

    private Long userId;
    private Long fromWalletId;
    private Long toWalletId;
    private Long categoryId;
    private String title;
    private String notes;
    private BigDecimal amount;
    private TransactionType type;
    private boolean softDeleted;

}
