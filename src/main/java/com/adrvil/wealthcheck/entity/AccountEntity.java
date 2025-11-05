package com.adrvil.wealthcheck.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class AccountEntity extends BaseEntity {
    private String name;
    private String email;
    private String providerId;
    private String avatarUrl;
    private boolean isActive;
}
