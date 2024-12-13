package com.damworks.backupsyncutility.backup;

import com.damworks.backupsyncutility.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Manages the database backup and its transfers
 */
/**
 * Manages the database dump process and ensures the backup directory exists.
 */
public class BackupManager {

    private static final Logger logger = LoggerFactory.getLogger(BackupManager.class);

    /**
     * Executes the dump process for all configured databases.
     *
     * @return Array of file paths for the generated dump files.
     * @throws IOException          If there is an issue with directory creation or dump process.
     * @throws InterruptedException If the dump process is interrupted.
     */
    public static String[] executeDump() throws IOException, InterruptedException {
        String[] databases = AppConfig.getDatabases();
        String localBackupPath = AppConfig.getLocalBackupPath();

        // Ensure the backup directory exists
        createBackupDirectory(localBackupPath);

        String[] dumpFiles = new String[databases.length];

        for (int i = 0; i < databases.length; i++) {
            String database = databases[i];
            logger.info("Starting dump for database: {}", database);

            dumpFiles[i] = DatabaseDumper.dump(
                    AppConfig.getDatabaseHost(),
                    AppConfig.getDatabasePort(),
                    AppConfig.getDatabaseUser(),
                    AppConfig.getDatabasePassword(),
                    database,
                    localBackupPath
            );

            logger.info("Database dump completed for: {}", database);
        }

        return dumpFiles;
    }

    /**
     * Ensures the local backup directory exists, creating it if necessary.
     *
     * @param path The path to the local backup directory.
     * @throws IOException If the directory cannot be created.
     */
    private static void createBackupDirectory(String path) throws IOException {
        File directory = new File(path);

        if (!directory.exists()) {
            logger.info("Backup directory does not exist. Attempting to create: {}", path);
            if (!directory.mkdirs()) {
                throw new IOException("Failed to create backup directory: " + path);
            }
            logger.info("Backup directory created successfully: {}", path);
        } else if (!directory.isDirectory()) {
            throw new IOException("Backup path exists but is not a directory: " + path);
        }
    }
}
