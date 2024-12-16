package com.damworks.backupsyncutility.sync;

import com.damworks.backupsyncutility.auth.GoogleDriveAuth;
import com.damworks.backupsyncutility.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Handles the synchronization of backup files across protocols (e.g., FTP, Google Drive).
 */
public class SyncManager {
    private static final Logger logger = LoggerFactory.getLogger(SyncManager.class);

    /**
     * Synchronizes the given files to configured protocols.
     *
     * @param dumpFiles Array of file paths to synchronize.
     */
    public static void syncFiles(String[] dumpFiles) {
        try {
            syncFTP(dumpFiles);
            syncGoogleDrive(dumpFiles);
        } catch (IOException e) {
            logger.error("Failed to synchronize files: {}", e.getMessage());
        }
    }

    /**
     * Synchronizes files via FTP.
     *
     * @param dumpFiles Array of file paths.
     * @throws IOException If an error occurs during upload.
     */
    private static void syncFTP(String[] dumpFiles) throws IOException {
        FTPHandler ftpHandler = new FTPHandler(
                AppConfig.getFTPServer(),
                AppConfig.getFTPPort(),
                AppConfig.getFTPUser(),
                AppConfig.getFTPPassword()
        );

        for (String dumpFile : dumpFiles) {
            File file = new File(dumpFile);

            String database = file.getParentFile().getName();
            String remotePath = AppConfig.getFTPRemotePath() + "/" + database;
            String remoteFilePath = remotePath + "/" + file.getName();

            ftpHandler.upload(dumpFile, remoteFilePath);
            logger.info("File synchronized to FTP: {}", remoteFilePath);
        }

        ftpHandler.close();
    }

    /**
     * Synchronizes files via Google Drive.
     *
     * @param dumpFiles Array of file paths.
     */
    private static void syncGoogleDrive(String[] dumpFiles) {
        try {
            String credentialsFile = AppConfig.getGoogleDriveCredentialsFile();
            String parentFolderId = AppConfig.getGoogleDriveFolderId();

            GoogleDriveHandler driveHandler = new GoogleDriveHandler(GoogleDriveAuth.getDriveService(credentialsFile));

            for (String dumpFile : dumpFiles) {
                driveHandler.uploadFile(dumpFile, parentFolderId);
                logger.info("File synchronized to Google Drive: {}", dumpFile);
            }
        } catch (Exception e) {
            logger.error("Failed to synchronize files to Google Drive: {}", e.getMessage());
        }
    }
}
