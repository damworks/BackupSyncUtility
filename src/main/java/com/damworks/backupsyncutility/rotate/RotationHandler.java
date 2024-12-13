package com.damworks.backupsyncutility.rotate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Handles file rotation to maintain only the latest N files.
 */
public class RotationHandler {
    private static final Logger logger = LoggerFactory.getLogger(RotationHandler.class);

    public static void rotateFiles(String directoryPath, int retentionDays) throws IOException {
        File directory = new File(directoryPath);
        if (!directory.exists() || !directory.isDirectory()) {
            throw new IOException("Invalid backup directory: " + directoryPath);
        }

        File[] files = directory.listFiles();
        if (files == null) return;

        Instant cutoffDate = Instant.now().minus(retentionDays, ChronoUnit.DAYS);

        for (File file : files) {
            BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            Instant fileCreationTime = attrs.creationTime().toInstant();

            if (fileCreationTime.isBefore(cutoffDate)) {
                if (file.delete()) {
                    logger.info("Deleted old backup file: {}", file.getName());
                } else {
                    logger.warn("Failed to delete old backup file: {}", file.getName());
                }
            }
        }
    }
}
