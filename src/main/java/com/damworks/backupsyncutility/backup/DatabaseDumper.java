package com.damworks.backupsyncutility.backup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * Handles the creation of database dumps using mysqldump.
 */
public class DatabaseDumper {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseDumper.class);

    /**
     * Dumps the specified database to a file.
     *
     * @param host       Host of the database server.
     * @param port       Port of the database server.
     * @param user       Database username.
     * @param password   Database password.
     * @param database   Name of the database to dump.
     * @param backupPath Directory where the dump file will be stored.
     * @return Path to the created dump file.
     * @throws IOException          If an I/O error occurs.
     * @throws InterruptedException If the process is interrupted.
     */
    public static String dump(String host, String port, String user, String password, String database, String backupPath)
            throws IOException, InterruptedException {
        // Generate a timestamped filename
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String dumpFileName = String.format("%s_%s.sql", database, timestamp);
        String dumpFilePath = backupPath + "/" + dumpFileName;

        logger.info("Creating database dump for '{}@{}:{}' in file: {}", database, host, port, dumpFilePath);

        // Build the mysqldump command
        String command = String.format(
                "mysqldump -h%s -P%s -u%s -p%s %s -r %s",
                host, port, user, password, database, dumpFilePath
        );

        // Execute the command
        Process process = Runtime.getRuntime().exec(command);

        // Wait for the process to complete
        int exitCode = process.waitFor();
        if (exitCode == 0) {
            logger.info("Database dump created successfully: {}", dumpFilePath);
        } else {
            throw new IOException("mysqldump failed with exit code: " + exitCode);
        }

        return dumpFilePath;
    }
}
