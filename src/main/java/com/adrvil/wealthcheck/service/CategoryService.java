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
}
