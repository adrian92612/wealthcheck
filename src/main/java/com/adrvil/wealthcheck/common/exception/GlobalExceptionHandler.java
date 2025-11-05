package com.adrvil.wealthcheck.common.exception;

import com.adrvil.wealthcheck.common.api.ApiResponseEntity;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.validation.FieldError;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFound.class)
    public ApiResponseEntity<Void> handleCreationExceptions(ResourceNotFound ex) {
        log.error("Resource not found: ", ex);
        return ApiResponseEntity.error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> Optional.ofNullable(fieldError.getDefaultMessage()).orElse("Invalid value"),
                        (msg1, msg2) -> msg1 + "; " + msg2
                ));

        return ApiResponseEntity.error(HttpStatus.BAD_REQUEST, "Validation failed", errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResponseEntity<Void> handleJacksonDeserialization(HttpMessageNotReadableException ex) {
        log.error(ex.getMessage());
        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException invalidFormatEx &&
                invalidFormatEx.getTargetType().isEnum()) {

            String value = invalidFormatEx.getValue().toString();
            String allowed = String.join(", ",
                    Arrays.stream(invalidFormatEx.getTargetType().getEnumConstants())
                            .map(Object::toString)
                            .toArray(String[]::new)
            );
            return ApiResponseEntity.error(
                    HttpStatus.BAD_REQUEST,
                    "Invalid value for " + invalidFormatEx.getPath().getFirst().getFieldName()
                            + ": " + value + ". Allowed values: " + allowed
            );
        }

        return ApiResponseEntity.error(HttpStatus.BAD_REQUEST, "Invalid request payload");
    }

    @ExceptionHandler(DataAccessException.class)
    public ApiResponseEntity<Void> handleDatabaseExceptions(DataAccessException ex) {
        log.error("Database error: ", ex);
        return ApiResponseEntity.error(HttpStatus.INTERNAL_SERVER_ERROR, "Database error");
    }

    @ExceptionHandler(AccountNotAuthenticatedException.class)
    public ApiResponseEntity<Void> handleCreationExceptions(AccountNotAuthenticatedException ex) {
        log.error("Account not authenticated: ", ex);
        return ApiResponseEntity.error(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(GoogleAuthException.class)
    public ApiResponseEntity<Void> handleGoogleAuthException(GoogleAuthException ex) {
        log.error("Google auth error: ", ex);
        return ApiResponseEntity.error(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ApiResponseEntity<Void> handleGenericException(Exception ex) {
        log.error("Unhandled exception: ", ex);
        return ApiResponseEntity.error(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }
}

