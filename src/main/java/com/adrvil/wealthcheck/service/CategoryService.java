package com.adrvil.wealthcheck.service;

import com.adrvil.wealthcheck.common.exception.ResourceNotFound;
import com.adrvil.wealthcheck.converter.CategoryDtoMapper;
import com.adrvil.wealthcheck.dto.request.CategoryReq;
import com.adrvil.wealthcheck.dto.response.CategoryRes;
import com.adrvil.wealthcheck.entity.CategoryEntity;
import com.adrvil.wealthcheck.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final AccountService accountService;
    private final CategoryMapper categoryMapper;

    public CategoryRes createCategory(CategoryReq req) {
        Long userId = accountService.getCurrentAccountIdOrThrow();
        CategoryEntity categoryEntity = CategoryDtoMapper.toEntity(req, userId);
        categoryMapper.insertCategory(categoryEntity);
        return CategoryDtoMapper.toDto(categoryEntity);
    }

    public CategoryRes updateCategory(Long id, CategoryReq req) {
        Long userId = accountService.getCurrentAccountIdOrThrow();
        int i = categoryMapper.updateCategory(id, userId, req);
        if (i == 0) throw new ResourceNotFound("Category");
        return CategoryDtoMapper.toDto(categoryMapper.getCategoryByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFound("Category")));
    }

    public CategoryRes getCategory(Long id) {
        Long userId = accountService.getCurrentAccountIdOrThrow();
        return CategoryDtoMapper.toDto(categoryMapper.getCategoryByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFound("Category")));
    }

    public List<CategoryRes> getAllCategories() {
        Long userId = accountService.getCurrentAccountIdOrThrow();
        List<CategoryEntity> categoryEntityList = categoryMapper.getAllCategoryByUserId(userId);
        return categoryEntityList.stream()
                .map(CategoryDtoMapper::toDto)
                .toList();
    }

    public CategoryRes deleteCategory(Long id) {
        Long userId = accountService.getCurrentAccountIdOrThrow();
        CategoryEntity categoryEntity = categoryMapper.getCategoryByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFound("Category"));
        int i = categoryMapper.softDeleteCategory(id, userId);
        if (i == 0) throw new ResourceNotFound("Category");
        return CategoryDtoMapper.toDto(categoryEntity);
    }
}
