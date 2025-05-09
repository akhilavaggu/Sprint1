package com.ResumeScreening.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.List;
import java.util.Map;

public class GoogleDriveHelper {

    
     //Downloads a file from Google Drive using its file ID.
     
    
    public static String downloadFileFromGoogleDrive(String fileId) throws IOException {
        // Construct the file download URL using the provided file ID
        String fileUrl = "https://drive.google.com/uc?export=download&id=" + fileId;

        // Attempt to retrieve the original filename from Google Drive
        String originalFilename = getOriginalFilename(fileId);
        
        // If filename is not found, use a default name for the downloaded file
        if (originalFilename == null || originalFilename.isEmpty()) {
            originalFilename = "resume_from_gdrive.pdf";
        }

        
        Path outputDir = Paths.get("resumes");
        
        // Create the directory if it doesn't already exist
        Files.createDirectories(outputDir);
        
        // Define the full output path for the downloaded file
        Path outputPath = outputDir.resolve(originalFilename);

        // Download the file from Google Drive
        try (InputStream in = new URL(fileUrl).openStream()) {
            // Copy the input stream (file data) to the specified output file
            Files.copy(in, outputPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("✅ Resume downloaded successfully to: " + outputPath.toAbsolutePath());
        }

        // Return the absolute file path of the saved file
        return outputPath.toString();
    }

    /**
     * Retrieves the original filename of a file from Google Drive based on its file ID.
     
     */
    private static String getOriginalFilename(String fileId) throws IOException {
        // Construct the URL for the Google Drive file
        URL url = new URL("https://drive.google.com/uc?export=download&id=" + fileId);
        
        // Open a connection to the URL and configure it for handling redirects
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setInstanceFollowRedirects(false);  // Disable automatic redirect following
        connection.connect();  // Establish the connection

        // Retrieve the response headers to look for the "Content-Disposition" header
        Map<String, List<String>> headers = connection.getHeaderFields();
        List<String> contentDisposition = headers.get("Content-Disposition");

        // If the "Content-Disposition" header exists, extract the filename from it
        if (contentDisposition != null && !contentDisposition.isEmpty()) {
            String value = contentDisposition.get(0);
            // Look for the filename parameter within the header's value
            if (value.contains("filename=")) {
                return value.split("filename=")[1].replace("\"", "").trim();
            }
        }
        
        return null;
    }
}
