package com.ResumeScreening;

import java.util.List;
import java.util.Scanner;

import com.ResumeScreening.entity.Recommendation;
import com.ResumeScreening.entity.Skill;
import com.ResumeScreening.service.*;

public class App {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Initialize all required services
        ResumeScreeningService resumeScreeningService = new ResumeScreeningService();
        ResumeService resumeService = new ResumeService();
        SkillService skillService = new SkillService();
        ResumeScoringService scoringService = new ResumeScoringService();
        RecommendationService recommendationService = new RecommendationService();
        UserService userService = new UserService(); // ✅ User registration and retrieval

        // Main menu loop
        while (true) {
            System.out.println("\n==== AI Resume Screening ====");
            System.out.println("1. Create Account");
            System.out.println("2. Upload Resume and View Resume Screening");
            System.out.println("3. Give Feedback on Resume");
            System.out.println("4. Exit");
            System.out.print("Choose an option: ");

            int choice = getValidIntegerInput(scanner);

            switch (choice) {
                case 1:
                    // Create new user account
                    userService.createAccount(scanner);
                    break;

                case 2:
                    // Resume screening process
                    System.out.print("Enter your email: ");
                    String email = scanner.nextLine().trim();

                    // Check if the user is registered
                    if (!isUserRegistered(email, userService)) {
                        System.out.println("❌ Unauthorized user. Please create an account first.");
                        break;
                    }

                    // Choose file upload source: system or Google Drive
                    System.out.print("Do you want to upload from System path or Google Drive? (1-System, 2-GDrive): ");
                    int inputType = getValidIntegerInput(scanner);
                    String filePath = "";

                    if (inputType == 1) {
                        // Local system upload
                        System.out.print("Enter full system file path: ");
                        filePath = scanner.nextLine();
                    } else if (inputType == 2) {
                        // Google Drive upload
                        System.out.print("Enter Google Drive File ID or URL: ");
                        String driveInput = scanner.nextLine().trim();
                        String fileId = extractFileIdFromUrl(driveInput);

                        if (fileId == null || fileId.isEmpty()) {
                            System.out.println("❌ Invalid Google Drive file ID.");
                            break;
                        }

                        String defaultDownloadPath = "downloads/resume_from_drive.pdf";
                        filePath = resumeService.downloadFileFromGoogleDrive(fileId, defaultDownloadPath);

                        if (filePath != null) {
                            System.out.println("✅ Resume downloaded from Google Drive to: " + filePath);
                        } else {
                            System.out.println("❌ Download failed.");
                            break;
                        }
                    } else {
                        System.out.println("❌ Invalid input type.");
                        break;
                    }

                    // If file path is invalid, cancel
                    if (filePath == null || filePath.isEmpty()) {
                        System.out.println("❌ File path is invalid. Upload canceled.");
                        break;
                    }

                    // Extract resume text using Apache Tika
                    String extractedText = resumeService.extractTextFromFile(filePath);

                    if (!extractedText.isEmpty()) {
                        // Save resume data to DB
                        resumeScreeningService.uploadResume(email, extractedText);

                        // Extract skills from resume text
                        List<Skill> skills = skillService.extractSkillsFromText(extractedText);

                        // Score resume based on extracted skills
                        int score = scoringService.calculateResumeScore(skills);

                        System.out.println("\n==== Screening Result for " + email + " ====");
                        System.out.println("Resume Score: " + score);
                        System.out.println("Extracted Skills: " + skills);

                        // Update resume record with score and text
                        Long resumeId = resumeService.getResumeIdByEmail(email);
                        if (resumeId != null && !extractedText.isEmpty()) {
                            resumeService.updateResumeDetails(resumeId, extractedText, score);
                        }

                        // Generate and display job recommendation if skills are found
                        if (!skills.isEmpty()) {
                            Recommendation rec = recommendationService.generateAIRecommendation(
                                    email, skills, extractedText, resumeId);
                            try {
                                recommendationService.saveRecommendation(rec);
                                System.out.println("\n==== AI-Based Job Recommendation ====");
                                System.out.println("Job Title: " + rec.getJobTitle());
                                System.out.println("Description: " + 
                                    (rec.getJobDescription() != null ? rec.getJobDescription() : "No description available."));
                            } catch (Exception e) {
                                System.err.println("❌ Failed to save recommendation: " + e.getMessage());
                            }
                        }
                    } else {
                        System.out.println("❗ Resume text is empty. Upload failed.");
                    }
                    break;

                case 3:
                    // Collect feedback on resume analysis
                    System.out.print("Enter your Email: ");
                    String feedbackEmail = scanner.nextLine().trim();

                    if (!isUserRegistered(feedbackEmail, userService)) {
                        System.out.println("❌ Unauthorized user. Please create an account first.");
                        break;
                    }

                    resumeScreeningService.giveFeedback(scanner, feedbackEmail);
                    break;

                case 4:
                    // Exit the application
                    System.out.println("Exiting... Thank you for using AI Resume Screening.");
                    scanner.close();
                    System.exit(0);
                    break;

                default:
                    System.out.println("Invalid option! Please try again.");
            }
        }
    }

    /**
     * Utility method to safely read integer input from scanner
     */
    private static int getValidIntegerInput(Scanner scanner) {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Invalid input! Please enter a number: ");
            }
        }
    }

    /**
     * Extracts Google Drive file ID from a given URL or returns direct ID
     */
    private static String extractFileIdFromUrl(String input) {
        if (input.contains("drive.google.com")) {
            try {
                String[] parts = input.split("/d/");
                if (parts.length > 1) {
                    return parts[1].split("/")[0];
                }
            } catch (Exception e) {
                return null;
            }
        }
        return input; // Treat as direct file ID
    }

    /**
     * Checks whether a user is already registered using their email
     */
    private static boolean isUserRegistered(String email, UserService userService) {
        return userService.getUserByEmail(email) != null;
    }
}
