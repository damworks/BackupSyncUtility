package com.damworks.backupsyncutility.rotate;

import com.damworks.backupsyncutility.auth.GoogleDriveAuth;
import com.damworks.backupsyncutility.config.AppConfig;
import com.damworks.backupsyncutility.sync.FTPHandler;
import com.damworks.backupsyncutility.sync.GoogleDriveHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles file rotation locally and for remote protocols (e.g., FTP, Google Drive).
 */
public class RotateManager {
    private static final Logger logger = LoggerFactory.getLogger(RotateManager.class);

    /**
     * Rotates all backups: local and for each protocol configured.
     */
    public static void rotateFiles() {
        // Step 1: Rotate locally and gather files to keep
        Map<String, String[]> filesToKeep = rotateLocal();

        // Step 2: Propagate rotation to FTP
        rotateFTP(filesToKeep);

        // Step 3: Propagate rotation to Google Drive
        rotateGoogleDrive(filesToKeep);
    }

    /**
     * Rotates local backup files and returns a map of files to keep for each database.
     *
     * @return A map where the key is the database name, and the value is an array of file names to keep.
     */
    private static Map<String, String[]> rotateLocal() {
        Map<String, String[]> filesToKeepMap = new HashMap<>();
        String baseBackupPath = AppConfig.getLocalBackupPath();

        File baseDirectory = new File(baseBackupPath);
        if (!baseDirectory.exists() || !baseDirectory.isDirectory()) {
            logger.error("Base backup directory is invalid: {}", baseBackupPath);
            return filesToKeepMap;
        }

        File[] databaseDirectories = baseDirectory.listFiles(File::isDirectory);
        if (databaseDirectories == null || databaseDirectories.length == 0) {
            logger.info("No database directories found for rotation.");
            return filesToKeepMap;
        }

        for (File databaseDirectory : databaseDirectories) {
            String databaseName = databaseDirectory.getName();

            try {
                File[] localFiles = databaseDirectory.listFiles(File::isFile);
                if (localFiles == null || localFiles.length == 0) {
                    logger.warn("No local files found for database: {}", databaseName);
                    continue;
                }

                // Sort files by last modified date (most recent first)
                Arrays.sort(localFiles, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));

                // Identify files to keep
                String[] filesToKeep = Arrays.stream(localFiles)
                        .limit(AppConfig.getRemoteFileRetentionCount())
                        .map(File::getName)
                        .toArray(String[]::new);

                // Rotate local files
                RotationHandler.rotateFiles(databaseDirectory.getAbsolutePath(), AppConfig.getRemoteFileRetentionCount());

                // Store the files to keep for this database
                filesToKeepMap.put(databaseName, filesToKeep);

            } catch (IOException e) {
                logger.error("Error during rotation for database {}: {}", databaseName, e.getMessage());
            }
        }

        logger.info("Local file rotation completed.");
        return filesToKeepMap;
    }

    /**
     * Rotates backup files on the FTP server, keeping only the files specified.
     *
     * @param filesToKeepMap A map where the key is the database name, and the value is an array of file names to keep.
     */
    private static void rotateFTP(Map<String, String[]> filesToKeepMap) {
        try {
            FTPHandler ftpHandler = new FTPHandler(
                    AppConfig.getFTPServer(),
                    AppConfig.getFTPPort(),
                    AppConfig.getFTPUser(),
                    AppConfig.getFTPPassword()
            );

            for (Map.Entry<String, String[]> entry : filesToKeepMap.entrySet()) {
                String databaseName = entry.getKey();
                String[] filesToKeep = entry.getValue();
                String remotePath = AppConfig.getFTPRemotePath() + "/" + databaseName;

                String[] remoteFiles = ftpHandler.listFiles(remotePath);

                if (remoteFiles == null || remoteFiles.length == 0) {
                    logger.warn("No files found on FTP for database: {}", databaseName);
                    continue;
                }

                // Delete files that are not in the list of files to keep
                for (String remoteFile : remoteFiles) {
                    if (!Arrays.asList(filesToKeep).contains(remoteFile)) {
                        ftpHandler.deleteFile(remotePath + "/" + remoteFile);
                        logger.info("Deleted old file from FTP: {}/{}", databaseName, remoteFile);
                    }
                }
            }

            ftpHandler.close();
            logger.info("FTP rotation completed.");
        } catch (IOException e) {
            logger.error("Error during FTP rotation: {}", e.getMessage());
        }
    }

    /**
     * Rotates backup files on Google Drive, keeping only the files specified.
     *
     * @param filesToKeepMap A map where the key is the database name, and the value is an array of file names to keep.
     */
    private static void rotateGoogleDrive(Map<String, String[]> filesToKeepMap) {
        try {
            GoogleDriveHandler driveHandler = new GoogleDriveHandler(GoogleDriveAuth.getDriveService(AppConfig.getGoogleDriveCredentialsPath()));
            String baseFolderId = AppConfig.getGoogleDriveFolderId();

            for (Map.Entry<String, String[]> entry : filesToKeepMap.entrySet()) {
                String databaseName = entry.getKey();
                String[] filesToKeep = entry.getValue();

                // Find or create the folder for this database
                String folderId = driveHandler.getOrCreateFolder(databaseName, baseFolderId);

                // List files in the Google Drive folder
                List<com.google.api.services.drive.model.File> remoteFiles = driveHandler.listFiles(folderId);

                for (com.google.api.services.drive.model.File remoteFile : remoteFiles) {
                    if (!Arrays.asList(filesToKeep).contains(remoteFile.getName())) {
                        driveHandler.deleteFile(remoteFile.getId());
                        logger.info("Deleted old file from Google Drive: {}/{}", databaseName, remoteFile.getName());
                    }
                }
            }

            logger.info("Google Drive rotation completed.");
        } catch (Exception e) {
            logger.error("Error during Google Drive rotation: {}", e.getMessage());
        }
    }
}
