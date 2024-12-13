package com.damworks.backupsyncutility.sync;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Handles FTP operations for uploading, listing, and deleting files.
 */
public class FTPHandler {
    private static final Logger logger = LoggerFactory.getLogger(FTPHandler.class);

    private final String server;
    private final int port;
    private final String user;
    private final String password;
    private final FTPClient ftpClient;

    public FTPHandler(String server, int port, String user, String password) throws IOException {
        this.server = server;
        this.port = port;
        this.user = user;
        this.password = password;

        ftpClient = new FTPClient();
        ftpClient.connect(server, port);
        ftpClient.login(user, password);
        ftpClient.enterLocalPassiveMode();
    }

    /**
     * Uploads a file to the FTP server.
     *
     * @param localFilePath  Path to the local file.
     * @param remoteFilePath Path on the FTP server.
     * @throws IOException If an I/O error occurs.
     */
    public void upload(String localFilePath, String remoteFilePath) throws IOException {
        FTPClient ftpClient = new FTPClient();

        try {
            logger.info("Connecting to FTP server: {}:{}", server, port);
            ftpClient.connect(server, port);
            ftpClient.login(user, password);

            // Set file type and transfer mode
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();

            logger.info("Starting upload of file: {}", localFilePath);
            try (InputStream inputStream = new FileInputStream(localFilePath)) {
                boolean success = ftpClient.storeFile(remoteFilePath, inputStream);
                if (success) {
                    logger.info("File uploaded successfully to {}", remoteFilePath);
                } else {
                    logger.error("Failed to upload file to {}", remoteFilePath);
                    throw new IOException("Failed to upload file to FTP server.");
                }
            }
        } finally {
            // Logout and disconnect
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.logout();
                    ftpClient.disconnect();
                    logger.info("Disconnected from FTP server.");
                } catch (IOException ex) {
                    logger.error("Error while disconnecting from FTP server: {}", ex.getMessage());
                }
            }
        }
    }

    /**
     * Lists files in a remote directory.
     *
     * @param remotePath The path to the remote directory.
     * @return Array of file names.
     * @throws IOException If an error occurs.
     */
    public String[] listFiles(String remotePath) throws IOException {
        FTPFile[] files = ftpClient.listFiles(remotePath);
        return Arrays.stream(files)
                .filter(FTPFile::isFile)
                .map(FTPFile::getName)
                .toArray(String[]::new);
    }

    /**
     * Deletes a file from the FTP server.
     *
     * @param remoteFilePath The full path to the file to delete.
     * @throws IOException If an error occurs.
     */
    public void deleteFile(String remoteFilePath) throws IOException {
        boolean success = ftpClient.deleteFile(remoteFilePath);
        if (!success) {
            throw new IOException("Failed to delete file: " + remoteFilePath);
        }
    }

    /**
     * Provides a comparator for sorting files by modification time.
     *
     * @param remotePath The remote path containing the files.
     * @return A comparator for FTP files.
     */
    public Comparator<String> getModificationTimeComparator(String remotePath) {
        return (file1, file2) -> {
            try {
                FTPFile[] files = ftpClient.listFiles(remotePath);
                FTPFile ftpFile1 = Arrays.stream(files).filter(f -> f.getName().equals(file1)).findFirst().orElse(null);
                FTPFile ftpFile2 = Arrays.stream(files).filter(f -> f.getName().equals(file2)).findFirst().orElse(null);
                if (ftpFile1 != null && ftpFile2 != null) {
                    return ftpFile1.getTimestamp().compareTo(ftpFile2.getTimestamp());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return 0;
        };
    }

    /**
     * Closes the FTP connection.
     *
     * @throws IOException If an error occurs.
     */
    public void close() throws IOException {
        if (ftpClient.isConnected()) {
            ftpClient.logout();
            ftpClient.disconnect();
        }
    }

}
