package com.adrvil.wealthcheck.common.exception;

public class TransactionCreationException extends RuntimeException {
    public TransactionCreationException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransactionCreationException(String message) {
        super(message);
    }
}
