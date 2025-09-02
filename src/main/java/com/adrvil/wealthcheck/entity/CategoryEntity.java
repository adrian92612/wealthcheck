package com.adrvil.wealthcheck.entity;

import com.adrvil.wealthcheck.enums.CategoryType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class CategoryEntity extends BaseEntity{
    Long userId;
    String name;
    String description;
    CategoryType type;
    String icon;
    boolean isActive;
}
