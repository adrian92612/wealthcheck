package com.adrvil.wealthcheck.common.exception;

public class ResourceNotFound extends RuntimeException {
    public ResourceNotFound(String resourceName) {
        super(resourceName + " not found");
    }
}
