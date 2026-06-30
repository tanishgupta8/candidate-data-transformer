package com.eightfold.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Utility class for common file system operations.
 */
public final class FileUtil {
    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

    private FileUtil() {}

    /**
     * Writes content to a file, ensuring parent directories are created.
     *
     * @param file the target File.
     * @param content the string content to write.
     * @throws IOException if writing fails.
     */
    public static void writeString(File file, String content) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }

        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            if (parent.mkdirs()) {
                logger.debug("Created parent directories: {}", parent.getAbsolutePath());
            }
        }

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
            logger.info("Successfully wrote content to: {}", file.getAbsolutePath());
        }
    }

    /**
     * Safely checks if a file exists and is not a directory.
     *
     * @param file the file to check.
     * @return true if valid file, false otherwise.
     */
    public static boolean isValidFile(File file) {
        return file != null && file.exists() && file.isFile();
    }
}
