package com.adrvil.wealthcheck.common.exception;

public class AccountNotAuthenticatedException extends RuntimeException {
    public AccountNotAuthenticatedException(String message) {
        super(message);
    }
}
