package com.adrvil.wealthcheck.entity;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Builder
public class AccountEntity extends BaseEntity{
    private String name;
    private String email;
    private String providerId;
    private String avatarUrl;
    private boolean isActive = true;
}
