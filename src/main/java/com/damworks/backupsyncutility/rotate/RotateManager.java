package com.damworks.backupsyncutility.rotate;

import com.damworks.backupsyncutility.config.AppConfig;
import com.damworks.backupsyncutility.sync.FTPHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * Handles file rotation locally and for remote protocols (e.g., FTP, Google Drive).
 */
public class RotateManager {
    private static final Logger logger = LoggerFactory.getLogger(RotateManager.class);
    private static final int remoteRetentionCount = AppConfig.getRemoteFileRetentionCount();

    /**
     * Rotates all backups: local and for each protocol configured.
     */
    public static void rotateFiles() {
        // Rotate locally
        rotateLocal();
        // Rotate on FTP
        rotateFTP();
        // toDo Placeholder for future protocols (e.g., Google Drive)
    }

    /**
     * Rotates local backup files.
     */
    public static void rotateLocal() {
        String baseBackupPath = AppConfig.getLocalBackupPath();

        File baseDirectory = new File(baseBackupPath);
        if (!baseDirectory.exists() || !baseDirectory.isDirectory()) {
            logger.error("Base backup directory is invalid: {}", baseBackupPath);
            return;
        }

        File[] databaseDirectories = baseDirectory.listFiles(File::isDirectory);
        if (databaseDirectories == null || databaseDirectories.length == 0) {
            logger.info("No database directories found for rotation in: {}", baseBackupPath);
            return;
        }

        for (File databaseDirectory : databaseDirectories) {
            try {
                RotationHandler.rotateFiles(databaseDirectory.getAbsolutePath(), remoteRetentionCount);
            } catch (IOException e) {
                logger.error("Error during rotation for directory {}: {}", databaseDirectory.getName(), e.getMessage());
            }
        }
        logger.info("Local file rotation completed.");
    }

    /**
     * Rotates backup files on the FTP server, keeping only the last N files.
     */
    public static void rotateFTP() {
        try {
            logger.info("Starting FTP rotation...");

            FTPHandler ftpHandler = new FTPHandler(
                    AppConfig.getFTPServer(),
                    AppConfig.getFTPPort(),
                    AppConfig.getFTPUser(),
                    AppConfig.getFTPPassword()
            );

            String baseRemotePath = AppConfig.getFTPRemotePath();
            String[] databaseDirectories = ftpHandler.listDirectories(baseRemotePath);

            if (databaseDirectories == null || databaseDirectories.length == 0) {
                logger.info("No database directories found on FTP for rotation.");
                return;
            }

            for (String databaseDirectory : databaseDirectories) {
                String databasePath = baseRemotePath + "/" + databaseDirectory;

                try {
                    String[] remoteFiles = ftpHandler.listFiles(databasePath);
                    if (remoteFiles == null || remoteFiles.length <= remoteRetentionCount) {
                        logger.info("No rotation needed for FTP directory: {}. Files found: {}", databasePath, remoteFiles != null ? remoteFiles.length : 0);
                        continue;
                    }

                    // Sort files by modification time (most recent first)
                    Arrays.sort(remoteFiles, ftpHandler.getModificationTimeComparator(databasePath).reversed());

                    // Delete files beyond retention count
                    for (int i = remoteRetentionCount; i < remoteFiles.length; i++) {
                        String fileToDelete = remoteFiles[i];
                        ftpHandler.deleteFile(databasePath + "/" + fileToDelete);
                        logger.info("Deleted old file from FTP directory {}: {}", databasePath, fileToDelete);
                    }
                } catch (IOException e) {
                    logger.error("Error during rotation for FTP directory {}: {}", databasePath, e.getMessage());
                }
            }

            ftpHandler.close();
            logger.info("FTP rotation completed. Retained last {} files per directory.", remoteRetentionCount);
        } catch (IOException e) {
            logger.error("Error during FTP rotation: {}", e.getMessage());
        }
    }
}
