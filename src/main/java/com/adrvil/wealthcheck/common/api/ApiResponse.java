package com.adrvil.wealthcheck.common.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.Instant;

@Data
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private int code;
    private String message;
    private T data;
    private Instant timestamp;

    public static <T> ApiResponse<T> success(HttpStatus statusCode, String message, T data) {
        return new ApiResponse<>(
                true,
                statusCode.value(),
                message,
                data,
                Instant.now()

        );
    }

    public static <T> ApiResponse<T> error(HttpStatus statusCode, String message, T data) {
        return new ApiResponse<>(
                false,
                statusCode.value(),
                message,
                data,
                Instant.now()

        );
    }
}
