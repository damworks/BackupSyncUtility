package com.damworks.backupsyncutility.rotate;

import com.damworks.backupsyncutility.config.AppConfig;
import com.damworks.backupsyncutility.sync.FTPHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;

/**
 * Handles file rotation locally and for remote protocols (e.g., FTP, Google Drive).
 */
public class RotateManager {
    private static final Logger logger = LoggerFactory.getLogger(RotateManager.class);

    /**
     * Rotates all backups: local and for each protocol configured.
     */
    public static void rotateFiles() {
        // Rotate locally
        rotateLocal();

        // Rotate on FTP
        rotateFTP();

        // Placeholder for future protocols (e.g., Google Drive)
    }

    /**
     * Rotates local backup files.
     */
    public static void rotateLocal() {
        try {
            String localBackupPath = AppConfig.getLocalBackupPath();
            RotationHandler.rotateFiles(localBackupPath, 1);
            logger.info("Local file rotation completed. Retained last 15 backups.");
        } catch (IOException e) {
            logger.error("Error during local file rotation: {}", e.getMessage());
        }
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

            String remotePath = AppConfig.getFTPRemotePath();
            int retentionDays = 15;

            // List files on the remote path
            String[] remoteFiles = ftpHandler.listFiles(remotePath);

            if (remoteFiles == null || remoteFiles.length == 0) {
                logger.info("No files found on FTP server for rotation.");
                return;
            }

            // Sort files by modification time
            Arrays.sort(remoteFiles, ftpHandler.getModificationTimeComparator(remotePath));

            // Remove older files, keeping the most recent ones
            int filesToRemove = Math.max(0, remoteFiles.length - retentionDays);
            for (int i = 0; i < filesToRemove; i++) {
                String fileToDelete = remoteFiles[i];
                ftpHandler.deleteFile(remotePath + "/" + fileToDelete);
                logger.info("Deleted old file from FTP: {}", fileToDelete);
            }

            logger.info("FTP rotation completed. Retained last {} files.", retentionDays);
        } catch (IOException e) {
            logger.error("Error during FTP rotation: {}", e.getMessage());
        }
    }
}
