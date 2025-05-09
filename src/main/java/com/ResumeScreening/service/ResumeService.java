package com.ResumeScreening.service;

import java.io.File;
import java.io.IOException;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;

import com.ResumeScreening.dao.ResumeDao;
import com.ResumeScreening.entity.Resume;

public class ResumeService {

    // Apache Tika instance to extract text from documents
    private Tika tika = new Tika();
    
    // DAO for interacting with resume data in the database
    private ResumeDao resumeDao = new ResumeDao();

    
     // Extract text from the resume file using Apache Tika.
     
    public String extractTextFromFile(String filePath) {
        try {
            File file = new File(filePath);
            
            // Check if the file exists
            if (!file.exists()) {
                System.out.println("❌ Error: File does not exist at " + filePath);
                return "";
            }

            // Use Tika to parse the file and extract its text
            String extractedText = tika.parseToString(file);

            // Check if extracted text is empty
            if (extractedText.isEmpty()) {
                System.out.println("⚠ Warning: Extracted text is empty!");
            } else {
                System.out.println("📄 Extracted Text:\n" + extractedText);
            }

            return extractedText;
        } catch (IOException | TikaException e) {
            // Handle errors during text extraction
            System.out.println("❌ Error extracting text from file: " + e.getMessage());
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Download a file from Google Drive using the file ID and save it to a given destination path.
     
     */
    public String downloadFileFromGoogleDrive(String fileId, String destinationPath) {
        try {
            // Ensure that the destination path is valid
            if (destinationPath == null || destinationPath.isEmpty()) {
                System.out.println("❌ Error: Destination path must be provided.");
                return null;
            }

            // Create the destination file and its parent directories if they do not exist
            File destFile = new File(destinationPath);
            File parent = destFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs(); // create parent folder if needed
            }

            // Use 'gdown' command to download the file from Google Drive
            String command = "gdown https://drive.google.com/uc?id=" + fileId + " -O " + destinationPath;
            System.out.println("⏬ Executing command: " + command);

            // Execute the download process
            Process process = Runtime.getRuntime().exec(command);
            int exitCode = process.waitFor(); // Wait for the process to finish

            // Check if the file was downloaded successfully
            if (exitCode == 0 && new File(destinationPath).exists()) {
                System.out.println("✅ File successfully downloaded to: " + destinationPath);
                return destinationPath;
            } else {
                System.out.println("❌ Download failed or file not found at " + destinationPath);
                return null;
            }

        } catch (Exception e) {
            // Handle any errors during the file download process
            System.out.println("❌ Failed to download file from Google Drive: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get the resume ID based on the user's email.
     */
    public Long getResumeIdByEmail(String email) {
        Resume resume = resumeDao.getResumeByEmail(email);
        return resume != null ? resume.getId() : null;
    }

    /**
     * Update an existing resume's details in the database.
     */
    public void updateResumeDetails(Long id, String extractedText, int resumeRank) {
        resumeDao.updateResumeDetails(id, extractedText, resumeRank);
    }

    /**
     * Process resume text and save it into the database.
     * This method either saves a new resume or updates an existing one based on the email.
     */
    public void processResumeTextAndSave(String email, String filePath, int resumeScore) {
        // Extract text from the provided file path
        String extractedText = extractTextFromFile(filePath);

        // If no text was extracted, return early
        if (extractedText.isEmpty()) {
            System.out.println("⚠ Failed to extract any text.");
            return;
        }

        // Check if the resume already exists for the given email
        Resume existing = resumeDao.getResumeByEmail(email);

        // If the resume doesn't exist, create a new one
        if (existing == null) {
            Resume resume = new Resume();
            resume.setEmail(email);
            resume.setFilePath(filePath);
            resume.setExtractedText(extractedText);
            resume.setRank(resumeScore);
            resumeDao.saveResume(resume);
            System.out.println("✅ New resume saved and analyzed.");
        } else {
            // Update the existing resume with the new text and score
            resumeDao.updateResumeDetails(existing.getId(), extractedText, resumeScore);
            System.out.println("✅ Existing resume updated with extracted text.");
        }
    }
}
