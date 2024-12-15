package com.damworks.backupsyncutility.backup;

import com.damworks.backupsyncutility.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the database dump process and ensures the backup directory exists.
 */
public class BackupManager {

    private static final Logger logger = LoggerFactory.getLogger(BackupManager.class);

    /**
     * Executes the database dump process and returns the list of generated dump files.
     *
     * @return Array of file paths for the generated dump files.
     * @throws IOException          If there is an issue with directory creation or dump process.
     * @throws InterruptedException If the dump process is interrupted.
     */
    public static String[] executeDump() throws IOException, InterruptedException {
        String[] databases = AppConfig.getDatabases();
        String localBackupPath = AppConfig.getLocalBackupPath();
        List<String> dumpFiles = new ArrayList<>();

        for (String database : databases) {
            logger.info("Starting dump for database: {}", database);

            // Ensure the database directory exists
            String databasePath = localBackupPath + File.separator + database;
            File databaseDir = new File(databasePath);
            if (!databaseDir.exists() && !databaseDir.mkdirs()) {
                throw new IOException("Failed to create directory for database: " + database);
            }

            // Execute the dump
            String dumpFile = DatabaseDumper.dump(
                    AppConfig.getDatabaseHost(),
                    AppConfig.getDatabasePort(),
                    AppConfig.getDatabaseUser(),
                    AppConfig.getDatabasePassword(),
                    database,
                    databasePath
            );

            logger.info("Dump completed for database: {}", database);
            dumpFiles.add(dumpFile);
        }

        return dumpFiles.toArray(new String[0]);
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
