package com.damworks.backupsyncutility.sync;

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
            // toDo: Add syncViaGoogleDrive(dumpFiles);
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
}
