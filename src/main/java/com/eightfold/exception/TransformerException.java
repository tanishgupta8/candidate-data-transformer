package com.eightfold.exception;

/**
 * Base exception for the Multi-Source Candidate Data Transformer pipeline.
 */
public class TransformerException extends RuntimeException {
    public TransformerException(String message) {
        super(message);
    }

    public TransformerException(String message, Throwable cause) {
        super(message, cause);
    }
}
