package com.eightfold.exception;

/**
 * Exception thrown when a candidate source document fails parsing.
 */
public class ParseException extends TransformerException {
    public ParseException(String message) {
        super(message);
    }

    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
