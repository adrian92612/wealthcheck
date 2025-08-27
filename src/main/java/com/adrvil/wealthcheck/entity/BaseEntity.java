package com.adrvil.wealthcheck.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BaseEntity {
    private long id;
    private Instant createdAt;
    private Instant updatedAt;
}
