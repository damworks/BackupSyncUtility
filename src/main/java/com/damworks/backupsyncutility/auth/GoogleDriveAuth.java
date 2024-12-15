package com.damworks.backupsyncutility.auth;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Provides authentication and access to the Google Drive API.
 */
public class GoogleDriveAuth {
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static HttpTransport HTTP_TRANSPORT;

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException("Failed to initialize HTTP transport for Google API: " + e.getMessage(), e);
        }
    }

    /**
     * Returns a Google Drive service instance using the provided credentials.
     *
     * @param credentialsPath Path to the service account credentials JSON file.
     * @return An authorized Drive API client service.
     * @throws IOException              If the credentials file cannot be read.
     * @throws GeneralSecurityException If the credentials are invalid.
     */
    public static Drive getDriveService(String credentialsPath) throws IOException, GeneralSecurityException {
        Credential credential = getCredentials(credentialsPath);

        return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName("BackupSyncUtility")
                .build();
    }

    /**
     * Loads the service account credentials from the given file path.
     *
     * @param credentialsPath Path to the service account credentials JSON file.
     * @return A Google Credential instance.
     * @throws IOException If the credentials file cannot be read.
     */
    private static Credential getCredentials(String credentialsPath) throws IOException {
        try (FileInputStream credentialsStream = new FileInputStream(credentialsPath)) {
            return GoogleCredential.fromStream(credentialsStream)
                    .createScoped(DriveScopes.all());
        }
    }
}
