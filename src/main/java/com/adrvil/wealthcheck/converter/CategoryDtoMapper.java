package com.adrvil.wealthcheck.converter;

import com.adrvil.wealthcheck.dto.request.CategoryReq;
import com.adrvil.wealthcheck.dto.response.CategoryRes;
import com.adrvil.wealthcheck.entity.CategoryEntity;

import java.time.Instant;

public class CategoryDtoMapper {
    public static CategoryEntity toEntity(CategoryReq req,Long userId) {
        return CategoryEntity.builder()
                .userId(userId)
                .name(req.name())
                .description(req.description())
                .type(req.type())
                .icon(req.icon())
                .isActive(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public static CategoryRes toDto(CategoryEntity entity) {
        return new CategoryRes(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getType(),
                entity.getIcon(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
