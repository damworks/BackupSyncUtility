package com.damworks.backupsyncutility.config;

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

    public static String getDatabasePort() {
        return ConfigLoader.getOrDefault("mysql.port", "3306");
    }

    public static String[] getDatabases() {
        return ConfigLoader.get("mysql.databases").split(",");
    }

    public static String getLocalBackupPath() {
        return ConfigLoader.getOrDefault("backup.local.path", "./backup");
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
}
