package com.eightfold.config;

import com.eightfold.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

/**
 * Loads runtime configurations from external files or classpath resources.
 */
public final class ConfigLoader {
    private static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);

    private ConfigLoader() {}

    /**
     * Loads a configuration map from a path (external file or classpath resource).
     * Falls back to default.json if load fails.
     *
     * @param path the path to the configuration file.
     * @return the configuration map.
     */
    public static Map<String, Object> loadConfig(String path) {
        if (path == null || path.isBlank()) {
            return loadDefaultConfig();
        }

        File file = new File(path);
        if (file.exists() && file.isFile()) {
            logger.info("Loading configuration from external file: {}", file.getAbsolutePath());
            try (InputStream is = new FileInputStream(file)) {
                return JsonUtil.toMap(is);
            } catch (Exception e) {
                logger.error("Failed to read external config file {}: {}", file.getName(), e.getMessage());
            }
        }

        // Try classpath resource
        logger.info("Looking for configuration in classpath: {}", path);
        try (InputStream is = ConfigLoader.class.getClassLoader().getResourceAsStream(path)) {
            if (is != null) {
                return JsonUtil.toMap(is);
            }
        } catch (Exception e) {
            logger.error("Failed to load configuration resource {}: {}", path, e.getMessage());
        }

        logger.warn("Configuration path '{}' not resolved. Falling back to default configuration.", path);
        return loadDefaultConfig();
    }

    /**
     * Loads the default configuration map.
     *
     * @return default configuration map.
     */
    public static Map<String, Object> loadDefaultConfig() {
        logger.info("Loading default configuration from classpath resource: config/default.json");
        try (InputStream is = ConfigLoader.class.getClassLoader().getResourceAsStream("config/default.json")) {
            if (is != null) {
                return JsonUtil.toMap(is);
            }
        } catch (Exception e) {
            logger.error("Critical: failed to load default.json config: {}", e.getMessage());
        }
        return Collections.emptyMap();
    }
}
