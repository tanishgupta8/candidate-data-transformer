package com.eightfold.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

/**
 * Utility class for JSON operations using Jackson.
 */
public final class JsonUtil {
    private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        // Pretty print output by default
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    private JsonUtil() {}

    /**
     * Converts an object into a pretty-printed JSON string.
     *
     * @param obj the object to serialize.
     * @return the JSON string.
     */
    public static String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            logger.error("Failed to serialize object to JSON: {}", e.getMessage());
            return "{}";
        }
    }

    /**
     * Parses a JSON file into a Map.
     *
     * @param file the JSON file.
     * @return the parsed Map, or an empty map on failure.
     */
    public static Map<String, Object> toMap(File file) {
        try {
            return mapper.readValue(file, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            logger.error("Failed to parse JSON file {} to Map: {}", file.getName(), e.getMessage());
            return Collections.emptyMap();
        }
    }

    /**
     * Parses a JSON InputStream into a Map.
     *
     * @param is the InputStream.
     * @return the parsed Map, or an empty map on failure.
     */
    public static Map<String, Object> toMap(InputStream is) {
        try {
            return mapper.readValue(is, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            logger.error("Failed to parse JSON stream to Map: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    /**
     * Returns the configured ObjectMapper instance.
     *
     * @return the ObjectMapper.
     */
    public static ObjectMapper getMapper() {
        return mapper;
    }
}
