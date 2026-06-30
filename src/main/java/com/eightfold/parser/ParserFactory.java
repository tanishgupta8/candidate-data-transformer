package com.eightfold.parser;

import java.io.File;

/**
 * Factory pattern implementation for selecting the correct parser based on file type.
 */
public final class ParserFactory {

    private ParserFactory() {}

    /**
     * Factory method to resolve and return the appropriate SourceParser.
     *
     * @param file the candidate file to parse.
     * @return the resolved parser.
     * @throws IllegalArgumentException if the file type is unsupported.
     */
    public static SourceParser getParser(File file) {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }

        String fileName = file.getName().toLowerCase();
        if (fileName.endsWith(".csv")) {
            return new RecruiterCsvParser();
        } else if (fileName.endsWith(".pdf")) {
            return new ResumePdfParser();
        } else {
            throw new IllegalArgumentException("Unsupported source file format: " + file.getName());
        }
    }
}
