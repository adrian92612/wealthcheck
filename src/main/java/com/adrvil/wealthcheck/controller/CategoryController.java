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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
