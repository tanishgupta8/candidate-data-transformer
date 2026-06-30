package com.eightfold.exception;

/**
 * Exception thrown when candidate schema validation fails.
 */
public class ValidationException extends TransformerException {
    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
