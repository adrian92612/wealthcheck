package com.adrvil.wealthcheck.common.exception;

public class InvalidTransactionRequestException extends RuntimeException {
    public InvalidTransactionRequestException(String message) {
        super(message);
    }
}
