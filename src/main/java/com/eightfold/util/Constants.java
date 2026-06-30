package com.eightfold.util;

import java.util.List;

/**
 * Global constant values used throughout the application.
 */
public final class Constants {

    private Constants() {}

    // Sources
    public static final String SOURCE_CSV = "Recruiter CSV";
    public static final String SOURCE_PDF = "Resume PDF";

    // Config Paths
    public static final String DEFAULT_CONFIG_PATH = "config/default.json";
    public static final String DEFAULT_SCHEMA_PATH = "schema/candidate-schema.json";

    // Default Source Priorities (Resume PDF has priority over Recruiter CSV)
    public static final List<String> DEFAULT_SOURCE_PRIORITIES = List.of(
            SOURCE_PDF,
            SOURCE_CSV
    );

    // Outputs
    public static final String DEFAULT_OUTPUT_PATH = "output/profile.json";
}
