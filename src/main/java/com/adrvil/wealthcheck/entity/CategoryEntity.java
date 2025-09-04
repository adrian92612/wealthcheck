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
public class CategoryEntity extends BaseEntity {
    private Long userId;
    private String name;
    private String description;
    private CategoryType type;
    private String icon;
    private boolean softDeleted;
}
