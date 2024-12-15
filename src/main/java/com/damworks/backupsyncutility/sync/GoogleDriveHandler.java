package com.damworks.backupsyncutility.sync;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Handles Google Drive operations like uploading files and creating folders.
 */
public class GoogleDriveHandler {
    private static final Logger logger = LoggerFactory.getLogger(GoogleDriveHandler.class);
    private final Drive driveService;

    public GoogleDriveHandler(Drive driveService) {
        this.driveService = driveService;
    }

    /**
     * Uploads a file to a Google Drive folder corresponding to the database.
     *
     * @param localFilePath   Path to the local file to upload.
     * @param parentFolderId  ID of the root folder for backups on Google Drive.
     * @throws IOException If an error occurs during upload.
     */
    public void uploadFile(String localFilePath, String parentFolderId) throws IOException {
        java.io.File localFile = new java.io.File(localFilePath);
        String databaseName = localFile.getParentFile().getName();

        // Ensure the folder for the database exists
        String databaseFolderId = getOrCreateFolder(databaseName, parentFolderId);

        // Upload the file to the database folder
        File fileMetadata = new File();
        fileMetadata.setName(localFile.getName());
        fileMetadata.setParents(Collections.singletonList(databaseFolderId));

        FileContent mediaContent = new FileContent("application/octet-stream", localFile);

        File uploadedFile = driveService.files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute();

        logger.info("Uploaded file '{}' to Google Drive folder '{}'", localFile.getName(), databaseName);
    }

    /**
     * Ensures a folder exists in Google Drive, creating it if necessary.
     *
     * @param folderName     Name of the folder.
     * @param parentFolderId ID of the parent folder.
     * @return ID of the folder.
     * @throws IOException If an error occurs.
     */
    public String getOrCreateFolder(String folderName, String parentFolderId) throws IOException {
        String folderId = findFolder(folderName, parentFolderId);
        if (folderId != null) {
            return folderId;
        }

        File folderMetadata = new File();
        folderMetadata.setName(folderName);
        folderMetadata.setMimeType("application/vnd.google-apps.folder");
        folderMetadata.setParents(Collections.singletonList(parentFolderId));

        File folder = driveService.files().create(folderMetadata)
                .setFields("id")
                .execute();

        logger.info("Created folder '{}' with ID: {}", folderName, folder.getId());
        return folder.getId();
    }

    /**
     * Finds a folder by name within a parent folder.
     *
     * @param folderName     Name of the folder to find.
     * @param parentFolderId ID of the parent folder.
     * @return ID of the folder if found, or null otherwise.
     * @throws IOException If an error occurs.
     */
    private String findFolder(String folderName, String parentFolderId) throws IOException {
        String query = String.format("mimeType='application/vnd.google-apps.folder' and name='%s' and '%s' in parents and trashed=false",
                folderName, parentFolderId);

        FileList result = driveService.files().list()
                .setQ(query)
                .setFields("files(id, name)")
                .execute();

        List<File> files = result.getFiles();
        return files.isEmpty() ? null : files.get(0).getId();
    }

    /**
     * Lists files in a Google Drive folder.
     *
     * @param parentFolderId Google Drive folder ID.
     * @return List of files in the folder.
     * @throws IOException If an error occurs.
     */
    public List<File> listFiles(String parentFolderId) throws IOException {
        String query = "'" + parentFolderId + "' in parents and trashed = false";
        FileList result = driveService.files().list()
                .setQ(query)
                .setFields("files(id, name, createdTime)")
                .execute();
        return result.getFiles();
    }

    /**
     * Deletes a file from Google Drive.
     *
     * @param fileId The ID of the file to delete.
     * @throws IOException If an error occurs.
     */
    public void deleteFile(String fileId) throws IOException {
        driveService.files().delete(fileId).execute();
        logger.info("File deleted from Google Drive: {}", fileId);
    }
}
