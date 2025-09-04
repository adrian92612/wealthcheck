package com.adrvil.wealthcheck.controller;

import com.adrvil.wealthcheck.common.api.ApiResponseEntity;
import com.adrvil.wealthcheck.dto.request.CategoryReq;
import com.adrvil.wealthcheck.dto.response.CategoryRes;
import com.adrvil.wealthcheck.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
        return ApiResponseEntity.success(HttpStatus.CREATED, "Category created", categoryService.createCategory(req));
    }

    @PutMapping("/{id}")
    public ApiResponseEntity<CategoryRes> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryReq req) {
        return ApiResponseEntity.success(HttpStatus.OK, "Category updated", categoryService.updateCategory(id, req));
    }

    @GetMapping("/{id}")
    public ApiResponseEntity<CategoryRes> getCategory(@PathVariable Long id) {
        return ApiResponseEntity.success(HttpStatus.FOUND, "Category found", categoryService.getCategory(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponseEntity<CategoryRes> deleteCategory(@PathVariable Long id) {
        return ApiResponseEntity.success(HttpStatus.OK, "Category deleted", categoryService.deleteCategory(id));
    }

    @GetMapping()
    public ApiResponseEntity<List<CategoryRes>> getAllCategory() {
        return ApiResponseEntity.success(HttpStatus.FOUND, "Category List", categoryService.getAllCategories());
    }


}
