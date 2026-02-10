package com.flashsell.infrastructure.data.exception;

/**
 * Bright Data API 异常
 */
public class BrightDataException extends RuntimeException {

    public BrightDataException(String message) {
        super(message);
    }

    public BrightDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
