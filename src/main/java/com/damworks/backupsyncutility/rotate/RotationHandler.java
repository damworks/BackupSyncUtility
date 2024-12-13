package com.damworks.backupsyncutility.rotate;

import com.damworks.backupsyncutility.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Handles file rotation to maintain only the latest N files.
 */
public class RotationHandler {
    private static final Logger logger = LoggerFactory.getLogger(RotationHandler.class);

    /**
     * Rotates files in the given directory to maintain only the latest N files.
     *
     * @param directoryPath The path to the directory to clean up.
     * @param retentionCount The number of most recent files to retain.
     * @throws IOException If an error occurs during rotation.
     */
    public static void rotateFiles(String directoryPath, int retentionCount) throws IOException {
        File directory = new File(directoryPath);
        if (!directory.exists() || !directory.isDirectory()) {
            throw new IOException("Invalid backup directory: " + directoryPath);
        }

        File[] files = directory.listFiles(File::isFile);
        if (files == null || files.length <= retentionCount) {
            logger.info("No rotation needed. Files in directory: {}", files != null ? files.length : 0);
            return;
        }

        // Sort files by creation time (most recent first)
        Arrays.sort(files, Comparator.comparingLong((File file) -> {
            try {
                return Files.readAttributes(file.toPath(), BasicFileAttributes.class).creationTime().toMillis();
            } catch (IOException e) {
                logger.warn("Could not get creation time for file: {}", file.getAbsolutePath());
                return Long.MAX_VALUE; // Push files with issues to the end
            }
        }).reversed());

        // Delete files beyond retention count
        for (int i = retentionCount; i < files.length; i++) {
            File fileToDelete = files[i];
            if (fileToDelete.delete()) {
                logger.info("Deleted old backup file: {}", fileToDelete.getName());
            } else {
                logger.error("Failed to delete old backup file: {}. Please check permissions or locks.", fileToDelete.getAbsolutePath());
            }
        }
    }
}
