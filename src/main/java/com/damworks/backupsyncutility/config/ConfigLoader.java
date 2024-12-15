package com.damworks.backupsyncutility.config;

import java.io.IOException;
import java.util.Properties;

/**
 * Utility class to load configuration properties from a file.
 */
public class ConfigLoader {
    private static final Properties properties = new Properties();

    static {
        try (var inputStream = ConfigLoader.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (inputStream == null) {
                throw new RuntimeException("Configuration file 'config.properties' not found in resources.");
            }
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration file.", e);
        }
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }

    public static String getOrDefault(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}
