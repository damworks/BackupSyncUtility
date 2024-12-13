package com.damworks.backupsyncutility;

import com.damworks.backupsyncutility.backup.BackupManager;
import com.damworks.backupsyncutility.rotate.RotateManager;
import com.damworks.backupsyncutility.sync.SyncManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class to execute the backup process.
 */
public class BackupSyncUtility {
    private static final Logger logger = LoggerFactory.getLogger(BackupSyncUtility.class);

    public static void main(String[] args) {
        logger.info("Starting the Backup and Synchronization process...");

        try {
            // Step 1: Dump databases locally
            String[] dumpFiles = BackupManager.executeDump();

            // Step 2: Synchronize files
            SyncManager.syncFiles(dumpFiles);

            // Step 3: Rotate files locally and on other protocols
            RotateManager.rotateFiles();

            logger.info("Backup and Synchronization process completed successfully.");
        } catch (Exception e) {
            logger.error("An error occurred during the Backup and Synchronization process: {}", e.getMessage());
        }
    }
}
