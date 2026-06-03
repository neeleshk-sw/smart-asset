package com.smartasset.common.exception;

public class SmartAssetException extends RuntimeException {
    private final String errorCode;

    public SmartAssetException(String message) {
        super(message);
        this.errorCode = "INTERNAL_ERROR";
    }

    public SmartAssetException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public SmartAssetException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "INTERNAL_ERROR";
    }

    public String getErrorCode() {
        return errorCode;
    }
}
