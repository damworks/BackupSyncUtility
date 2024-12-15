package com.damworks.backupsyncutility.auth;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
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
    private static final HttpTransport HTTP_TRANSPORT;

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
        // Load service account credentials
        ServiceAccountCredentials credentials = getCredentials(credentialsPath);

        return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, new com.google.auth.http.HttpCredentialsAdapter(credentials))
                .setApplicationName("BackupSyncUtility")
                .build();
    }

    /**
     * Loads the service account credentials from the given file path.
     *
     * @param credentialsPath Path to the service account credentials JSON file.
     * @return A ServiceAccountCredentials instance.
     * @throws IOException If the credentials file cannot be read.
     */
    private static ServiceAccountCredentials getCredentials(String credentialsPath) throws IOException {
        try (FileInputStream credentialsStream = new FileInputStream(credentialsPath)) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream)
                    .createScoped(DriveScopes.all());
            if (credentials instanceof ServiceAccountCredentials) {
                return (ServiceAccountCredentials) credentials;
            } else {
                throw new IOException("Provided credentials are not a service account.");
            }
        }
    }
}
