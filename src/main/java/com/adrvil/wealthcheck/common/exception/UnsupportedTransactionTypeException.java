package com.adrvil.wealthcheck.common.exception;

public class UnsupportedTransactionTypeException extends RuntimeException {
    public UnsupportedTransactionTypeException() {
        super("Unsupported transaction type");
    }
}
