package com.eightfold.exception;

/**
 * Exception thrown when output projection mapping configuration fails.
 */
public class ProjectionException extends TransformerException {
    public ProjectionException(String message) {
        super(message);
    }
}
