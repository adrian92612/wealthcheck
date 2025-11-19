package com.adrvil.wealthcheck.service;

import com.adrvil.wealthcheck.common.exception.ResourceNotFound;
import com.adrvil.wealthcheck.converter.CategoryDtoMapper;
import com.adrvil.wealthcheck.dto.request.CategoryReq;
import com.adrvil.wealthcheck.dto.response.CategoryRes;
import com.adrvil.wealthcheck.entity.CategoryEntity;
import com.adrvil.wealthcheck.enums.CacheName;
import com.adrvil.wealthcheck.enums.CategoryType;
import com.adrvil.wealthcheck.mapper.CategoryMapper;
import com.adrvil.wealthcheck.utils.CacheUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryService {

    private final AccountService accountService;
    private final CategoryMapper categoryMapper;
    private final CacheUtil cacheUtil;

    public CategoryRes createCategory(CategoryReq req, Long id) {
        Long userId = Optional.ofNullable(id).orElseGet(accountService::getCurrentAccountIdOrThrow);

        log.debug("Creating category for user: {}, name: {}, type: {}", userId, req.name(), req.type());

        CategoryEntity categoryEntity = CategoryDtoMapper.toEntity(req, userId);
        categoryMapper.insertCategory(categoryEntity);

        cacheUtil.evict(CacheName.USER_CATEGORIES.getValue(), String.valueOf(userId));
        cacheUtil.evictOverviewCaches(userId);

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

        cacheUtil.evict(CacheName.CATEGORY.getValue(), userId + ":" + id);
        cacheUtil.evict(CacheName.USER_CATEGORIES.getValue(), String.valueOf(userId));
        cacheUtil.evictOverviewCaches(userId);

        log.info("Category updated successfully - ID: {}, User: {}", id, userId);

        return CategoryDtoMapper.toDto(categoryMapper.getCategoryByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFound("Category")));
    }

    public CategoryRes restoreCategory(Long id) {
        Long userId = accountService.getCurrentAccountIdOrThrow();
        log.debug("Restoring category - ID: {}, User: {}", id, userId);

        Boolean isSoftDeleted = categoryMapper.isSoftDeleted(userId, id);
        if (isSoftDeleted == null) {
            log.warn("Category not found for restoration - ID: {}, User: {}", id, userId);
            throw new ResourceNotFound("Category");
        }

        if (!isSoftDeleted) {
            log.warn("Restore rejected - Category is NOT soft deleted. ID: {}, User: {}", id, userId);
            throw new IllegalStateException("Category is not soft deleted");
        }

        if (categoryMapper.restoreCategory(id, userId) == 0) {
            log.warn("Category restore failed - ID: {}, User: {}", id, userId);
            throw new ResourceNotFound("Category");
        }

        evictCategoryCaches(userId, id);

        log.info("Category restored successfully - ID: {}, User: {}", id, userId);
        return CategoryDtoMapper.toDto(categoryMapper.getCategoryByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFound("Category")));
    }

    public CategoryRes getCategory(Long id) {
        Long userId = accountService.getCurrentAccountIdOrThrow();
        String cacheKey = userId + ":" + id;

        CategoryRes cached = cacheUtil.get(CacheName.CATEGORY.getValue(), cacheKey);
        if (cached != null) {
            return cached;
        }

        log.debug("Fetching category - ID: {}, User: {}", id, userId);

        CategoryEntity category = categoryMapper.getCategoryByIdAndUserId(id, userId)
                .orElseThrow(() -> {
                    log.warn("Category not found - ID: {}, User: {}", id, userId);
                    return new ResourceNotFound("Category");
                });

        log.debug("Category found - ID: {}, Name: {}, Type: {}", id, category.getName(), category.getType());

        CategoryRes categoryRes = CategoryDtoMapper.toDto(category);

        cacheUtil.put(CacheName.CATEGORY.getValue(), cacheKey, categoryRes);

        return categoryRes;
    }

    public List<CategoryRes> getAllCategories() {
        Long userId = accountService.getCurrentAccountIdOrThrow();

        List<CategoryRes> cachedList = cacheUtil.get(CacheName.USER_CATEGORIES.getValue(), String.valueOf(userId));

        if (cachedList != null) return cachedList;

        log.debug("Fetching all categories for user: {}", userId);

        List<CategoryEntity> categoryEntityList = categoryMapper.getAllCategoryByUserId(userId, false);

        log.info("Returning {} categories for user: {}", categoryEntityList.size(), userId);

        List<CategoryRes> categoryResList = categoryEntityList.stream()
                .map(CategoryDtoMapper::toDto)
                .toList();

        cacheUtil.put(CacheName.USER_CATEGORIES.getValue(), String.valueOf(userId), categoryResList);

        return categoryResList;
    }

    public List<CategoryRes> getAllSoftDeletedCategories() {
        Long userId = accountService.getCurrentAccountIdOrThrow();
        List<CategoryRes> cachedList = cacheUtil.get(CacheName.DELETED_USER_CATEGORIES.getValue(), String.valueOf(userId));

        if (cachedList != null) return cachedList;

        log.debug("Fetching all soft deleted categories for user: {}", userId);
        List<CategoryEntity> categoryEntityList = categoryMapper.getAllCategoryByUserId(userId, true);

        log.info("Returning {} soft deleted categories for user: {}", categoryEntityList.size(), userId);

        List<CategoryRes> categoryResList = categoryEntityList.stream()
                .map(CategoryDtoMapper::toDto)
                .toList();

        cacheUtil.put(CacheName.DELETED_USER_CATEGORIES.getValue(), String.valueOf(userId), categoryResList);

        return categoryResList;
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

        evictCategoryCaches(userId, id);

        log.info("Category soft deleted successfully - ID: {}, User: {}, Name: {}",
                id, userId, categoryEntity.getName());

        return CategoryDtoMapper.toDto(categoryEntity);
    }

    public void permanentDeleteCategory(Long id) {
        Long userId = accountService.getCurrentAccountIdOrThrow();
        log.debug(
                "Permanently deleting category - ID: {}, User: {}",
                id, userId
        );

        Boolean isSoftDeleted = categoryMapper.isSoftDeleted(userId, id);

        if (isSoftDeleted == null) {
            log.warn("Category not found for permanent delete - ID: {}, User: {}", id, userId);
            throw new ResourceNotFound("Category");
        }

        if (!isSoftDeleted) {
            log.warn("Permanent delete rejected - Category is NOT soft deleted. ID: {}, User: {}", id, userId);
            throw new IllegalStateException("Category is not soft deleted");
        }

        int deleted = categoryMapper.permanentDeleteCategory(userId, id);
        if (deleted == 0) {
            log.error(
                    "Permanent delete FAILED - No rows affected. Possible concurrent operation. ID: {}, User: {}",
                    id, userId
            );
            throw new ResourceNotFound("Category");
        }

        evictCategoryCaches(userId, id);

        log.info(
                "Transaction permanently deleted - ID: {}, User: {}",
                id, userId
        );
    }

    public void createDefaultCategories(Long userId) {
        List<CategoryReq> defaultCategories = List.of(
                new CategoryReq("Food", null, CategoryType.EXPENSE, "food"),
                new CategoryReq("Utilities", null, CategoryType.EXPENSE, "plug"),
                new CategoryReq("Other Expenses", null, CategoryType.EXPENSE, "shoppingBag"),
                new CategoryReq("Initial Balance", null, CategoryType.INCOME, "cash"),
                new CategoryReq("Salary", null, CategoryType.INCOME, "wallet"),
                new CategoryReq("Other Incomes", null, CategoryType.INCOME, "coins")
        );

        log.info("Creating {} default categories for user ID: {}", defaultCategories.size(), userId);

        defaultCategories.forEach(req -> {
            try {
                CategoryRes category = createCategory(req, userId);
                log.info("Default category created: {} (ID: {}, Type: {}) for user {}",
                        category.name(), category.id(), category.type(), userId);
            } catch (Exception e) {
                log.error("Failed to create default category '{}' for user {}: {}",
                        req.name(), userId, e.getMessage(), e);
            }
        });

        log.info("Finished creating default categories for user ID: {}", userId);
    }

    // helper functions

    private void evictCategoryCaches(Long userId, Long categoryId) {
        cacheUtil.evict(CacheName.CATEGORY.getValue(), userId + ":" + categoryId);
        cacheUtil.evict(CacheName.USER_CATEGORIES.getValue(), String.valueOf(userId));
        cacheUtil.evict(CacheName.DELETED_USER_CATEGORIES.getValue(), String.valueOf(userId));
        cacheUtil.evictOverviewCaches(userId);
    }
}
