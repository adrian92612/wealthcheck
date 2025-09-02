package com.adrvil.wealthcheck.service;

import com.adrvil.wealthcheck.common.exception.CategoryCreationException;
import com.adrvil.wealthcheck.converter.CategoryDtoMapper;
import com.adrvil.wealthcheck.dto.request.CategoryReq;
import com.adrvil.wealthcheck.dto.response.CategoryRes;
import com.adrvil.wealthcheck.entity.CategoryEntity;
import com.adrvil.wealthcheck.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.javassist.NotFoundException;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final AccountService accountService;
    private final CategoryMapper categoryMapper;

    public CategoryRes createCategory(CategoryReq req) throws NotFoundException {
        Long userId = accountService.getCurrentAccountIdOrThrow();
        CategoryEntity categoryEntity = CategoryDtoMapper.toEntity(req, userId);
        try {
            categoryMapper.insertCategory(categoryEntity);
        } catch (PersistenceException | DataAccessException e) {
            throw new CategoryCreationException("Unable to create category", e);
        }
        return CategoryDtoMapper.toDto(categoryEntity);
    }

    public CategoryRes updateCategory(Long id, CategoryReq req) throws NotFoundException {
        Long userId = accountService.getCurrentAccountIdOrThrow();
        int i = categoryMapper.updateCategory(id, userId, req);
        if (i == 1) {
            return CategoryDtoMapper.toDto(categoryMapper.getCategoryByIdAndUserId(id, userId));
        }
        throw new NotFoundException("Unable to update category");
    }

    public CategoryRes getCategory(Long id) throws NotFoundException {
        Long userId = accountService.getCurrentAccountIdOrThrow();
        CategoryEntity categoryEntity = categoryMapper.getCategoryByIdAndUserId(id, userId);
        return CategoryDtoMapper.toDto(categoryEntity);
    }

    public List<CategoryRes> getAllCategories() throws NotFoundException {
        Long userId = accountService.getCurrentAccountIdOrThrow();
        List<CategoryEntity> categoryEntityList = categoryMapper.getAllCategoryByUserId(userId);
        return categoryEntityList.stream()
                .map(CategoryDtoMapper::toDto)
                .toList();
    }

    public void deleteCategory(Long id) throws NotFoundException {
        Long userId = accountService.getCurrentAccountIdOrThrow();
        int i = categoryMapper.softDeleteCategory(id, userId);
        if (i == 0) throw new NotFoundException("Unable to delete category");
    }
}
