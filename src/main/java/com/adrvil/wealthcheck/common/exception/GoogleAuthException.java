package com.adrvil.wealthcheck.common.exception;

public class GoogleAuthException extends Exception {
    public GoogleAuthException(String message) {
        super(message);
    }
    public GoogleAuthException(String message, Throwable cause) {
        super(message, cause);
    }
}
