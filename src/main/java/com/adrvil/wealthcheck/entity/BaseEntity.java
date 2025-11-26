package com.adrvil.wealthcheck.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class BaseEntity {
    private Long id;
    private Instant createdAt;
    private Instant updatedAt;
    private boolean softDeleted;
}
