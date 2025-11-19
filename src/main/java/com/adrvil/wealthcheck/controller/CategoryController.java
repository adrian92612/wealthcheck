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

    @GetMapping()
    public ApiResponseEntity<List<CategoryRes>> getAllCategory() {
        return ApiResponseEntity.success(HttpStatus.FOUND, "Category List", categoryService.getAllCategories());
    }

    @PostMapping()
    public ApiResponseEntity<CategoryRes> createCategory(@Valid @RequestBody CategoryReq req) {
        return ApiResponseEntity.success(HttpStatus.CREATED, "Category created", categoryService.createCategory(req, null));
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

    @GetMapping("/deleted")
    public ApiResponseEntity<List<CategoryRes>> getAllSoftDeletedCategory() {
        return ApiResponseEntity.success(HttpStatus.FOUND, "Deleted Category List", categoryService.getAllSoftDeletedCategories());
    }

    @DeleteMapping("/permanent-delete/{id}")
    public ApiResponseEntity<Void> deletePermanentCategory(@PathVariable Long id) {
        categoryService.permanentDeleteCategory(id);
        return ApiResponseEntity.success(HttpStatus.OK, "Category permanently deleted", null);
    }

    @PutMapping("/restore/{id}")
    public ApiResponseEntity<CategoryRes> restoreCategory(@PathVariable Long id) {
        return ApiResponseEntity.success(HttpStatus.OK, "Category restored", categoryService.restoreCategory(id));
    }

}
