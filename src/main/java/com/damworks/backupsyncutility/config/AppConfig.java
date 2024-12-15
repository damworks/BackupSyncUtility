package com.damworks.backupsyncutility.config;

import java.util.Objects;

/**
 * Provides application-level configuration.
 */
public class AppConfig {
    public static String getDatabaseUser() {
        return ConfigLoader.get("mysql.user");
    }

    public static String getDatabasePassword() {
        return ConfigLoader.get("mysql.password");
    }

    public static String getDatabaseHost() {
        return ConfigLoader.getOrDefault("mysql.host", "localhost");
    }

    public static int getDatabasePort() {
        String port = ConfigLoader.get("mysql.port");
        return port != null ? Integer.parseInt(port) : 3306;
    }

    public static String[] getDatabases() {
        return ConfigLoader.get("mysql.databases").split(",");
    }

    public static String getLocalBackupPath() {
        return ConfigLoader.getOrDefault("backup.local.path", "./backup");
    }

    public static int getRemoteFileRetentionCount() {
        String value = ConfigLoader.get("backup.file.retention.count");
        return Integer.parseInt(value);
    }

    // FTP configuration
    public static String getFTPServer() {
        return ConfigLoader.get("ftp.server");
    }

    public static int getFTPPort() {
        String port = ConfigLoader.get("ftp.port");
        return port != null ? Integer.parseInt(port) : 21;
    }

    public static String getFTPUser() {
        return ConfigLoader.get("ftp.user");
    }

    public static String getFTPPassword() {
        return ConfigLoader.get("ftp.password");
    }

    public static String getFTPRemotePath() {
        return ConfigLoader.get("ftp.remotePath");
    }

    public static String getGoogleDriveCredentialsFile() {
        String fileName = ConfigLoader.get("google.service.account.key");
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("googleDriveCredentialsFile is not configured.");
        }
        return Objects.requireNonNull(AppConfig.class.getClassLoader().getResource(fileName)).getFile();
    }

    public static String getGoogleDriveFolderId() {
        return ConfigLoader.get("googleDriveFolderId");
    }
}
