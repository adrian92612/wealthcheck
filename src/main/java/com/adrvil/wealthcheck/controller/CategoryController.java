package com.adrvil.wealthcheck.controller;

import com.adrvil.wealthcheck.common.api.ApiResponseEntity;
import com.adrvil.wealthcheck.common.exception.WalletCreationException;
import com.adrvil.wealthcheck.dto.request.CategoryReq;
import com.adrvil.wealthcheck.dto.response.CategoryRes;
import com.adrvil.wealthcheck.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.javassist.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping()
    public ApiResponseEntity<CategoryRes> createCategory(@Valid @RequestBody CategoryReq req) {
        try {
            CategoryRes categoryRes = categoryService.createCategory(req);
            return ApiResponseEntity.success(HttpStatus.CREATED, "Category created", categoryRes);
        } catch (NotFoundException e) {
            return ApiResponseEntity.error(HttpStatus.NOT_FOUND, e.getMessage(), null);
        } catch (WalletCreationException e) {
            return ApiResponseEntity.error(HttpStatus.BAD_REQUEST, e.getMessage(), null);
        }
    }

    @PutMapping("/{id}")
    public ApiResponseEntity<CategoryRes> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryReq req) {
        try {
            CategoryRes categoryRes = categoryService.updateCategory(id, req);
            return ApiResponseEntity.success(HttpStatus.OK, "Category updated", categoryRes);
        } catch (NotFoundException e) {
            return ApiResponseEntity.error(HttpStatus.NOT_FOUND, e.getMessage(), null);
        }
    }

    @GetMapping("/{id}")
    public ApiResponseEntity<CategoryRes> getCategory(@PathVariable Long id) {
        try {
            CategoryRes categoryRes = categoryService.getCategory(id);
            return ApiResponseEntity.success(HttpStatus.FOUND, "Category found", categoryRes);
        } catch (NotFoundException e) {
            return ApiResponseEntity.error(HttpStatus.NOT_FOUND, e.getMessage(), null);
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            return ApiResponseEntity.success(HttpStatus.OK, "Category deleted", null);
        } catch (NotFoundException e) {
            return ApiResponseEntity.error(HttpStatus.NOT_FOUND, e.getMessage(), null);
        }
    }

    @GetMapping("/all")
    public ApiResponseEntity<List<CategoryRes>> getAllCategory() {
        try {
            List<CategoryRes> categoryResList = categoryService.getAllCategories();
            return ApiResponseEntity.success(HttpStatus.FOUND, "Category List", categoryResList);
        } catch (NotFoundException e) {
            return ApiResponseEntity.error(HttpStatus.NOT_FOUND, e.getMessage(), null);
        }
    }



}
