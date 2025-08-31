package com.adrvil.wealthcheck.common.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ApiResponseEntity<T> extends ResponseEntity<ApiResponse<T>> {

    private ApiResponseEntity(ApiResponse<T> body, HttpStatus status) {
        super(body, status);
    }

    // --- Success with data ---
    public static <T> ApiResponseEntity<T> success(HttpStatus status, String message, T data) {
        return new ApiResponseEntity<>(
                ApiResponse.success(status, message, data),
                status
        );
    }

    // --- Success without data ---
    public static ApiResponseEntity<Void> success(HttpStatus status, String message) {
        return new ApiResponseEntity<>(
                ApiResponse.success(status, message, null),
                status
        );
    }

    // --- Error with data ---
    public static <T> ApiResponseEntity<T> error(HttpStatus status, String message, T data) {
        return new ApiResponseEntity<>(
                ApiResponse.error(status, message, data),
                status
        );
    }

    // --- Error without data ---
    public static ApiResponseEntity<Void> error(HttpStatus status, String message) {
        return new ApiResponseEntity<>(
                ApiResponse.error(status, message, null),
                status
        );
    }
}


