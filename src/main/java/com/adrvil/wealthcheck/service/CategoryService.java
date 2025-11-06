package com.adrvil.wealthcheck.service;

import com.adrvil.wealthcheck.common.exception.ResourceNotFound;
import com.adrvil.wealthcheck.converter.CategoryDtoMapper;
import com.adrvil.wealthcheck.dto.request.CategoryReq;
import com.adrvil.wealthcheck.dto.response.CategoryRes;
import com.adrvil.wealthcheck.entity.CategoryEntity;
import com.adrvil.wealthcheck.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryService {

    private final AccountService accountService;
    private final CategoryMapper categoryMapper;

    public CategoryRes createCategory(CategoryReq req) {
        Long userId = accountService.getCurrentAccountIdOrThrow();

        log.debug("Creating category for user: {}, name: {}, type: {}", userId, req.name(), req.type());

        CategoryEntity categoryEntity = CategoryDtoMapper.toEntity(req, userId);
        categoryMapper.insertCategory(categoryEntity);

        log.info("Category created successfully - ID: {}, User: {}, Name: {}, Type: {}",
                categoryEntity.getId(), userId, req.name(), req.type());

        return CategoryDtoMapper.toDto(categoryEntity);
    }

    public CategoryRes updateCategory(Long id, CategoryReq req) {
        Long userId = accountService.getCurrentAccountIdOrThrow();

        log.debug("Updating category - ID: {}, User: {}, New name: {}, New type: {}",
                id, userId, req.name(), req.type());

        int updated = categoryMapper.updateCategory(id, userId, req);
        if (updated == 0) {
            log.warn("Category update failed - ID: {}, User: {}", id, userId);
            throw new ResourceNotFound("Category");
        }

        log.info("Category updated successfully - ID: {}, User: {}", id, userId);

        return CategoryDtoMapper.toDto(categoryMapper.getCategoryByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFound("Category")));
    }

    public CategoryRes getCategory(Long id) {
        Long userId = accountService.getCurrentAccountIdOrThrow();

        log.debug("Fetching category - ID: {}, User: {}", id, userId);

        CategoryEntity category = categoryMapper.getCategoryByIdAndUserId(id, userId)
                .orElseThrow(() -> {
                    log.warn("Category not found - ID: {}, User: {}", id, userId);
                    return new ResourceNotFound("Category");
                });

        log.debug("Category found - ID: {}, Name: {}, Type: {}", id, category.getName(), category.getType());

        return CategoryDtoMapper.toDto(category);
    }

    public List<CategoryRes> getAllCategories() {
        Long userId = accountService.getCurrentAccountIdOrThrow();

        log.debug("Fetching all categories for user: {}", userId);

        List<CategoryEntity> categoryEntityList = categoryMapper.getAllCategoryByUserId(userId);

        log.info("Returning {} categories for user: {}", categoryEntityList.size(), userId);

        return categoryEntityList.stream()
                .map(CategoryDtoMapper::toDto)
                .toList();
    }

    public CategoryRes deleteCategory(Long id) {
        Long userId = accountService.getCurrentAccountIdOrThrow();

        log.debug("Soft deleting category - ID: {}, User: {}", id, userId);

        CategoryEntity categoryEntity = categoryMapper.getCategoryByIdAndUserId(id, userId)
                .orElseThrow(() -> {
                    log.warn("Category not found for deletion - ID: {}, User: {}", id, userId);
                    return new ResourceNotFound("Category");
                });

        int deleted = categoryMapper.softDeleteCategory(id, userId);
        if (deleted == 0) {
            log.warn("Category soft delete failed - ID: {}, User: {}", id, userId);
            throw new ResourceNotFound("Category");
        }

        log.info("Category soft deleted successfully - ID: {}, User: {}, Name: {}",
                id, userId, categoryEntity.getName());

        return CategoryDtoMapper.toDto(categoryEntity);
    }
}
