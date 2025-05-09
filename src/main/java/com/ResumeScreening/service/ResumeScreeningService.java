package com.ResumeScreening.service;

import com.ResumeScreening.dao.*;
import com.ResumeScreening.entity.*;
import com.ResumeScreening.util.ResumeRanker;
import com.ResumeScreening.util.GoogleDriveHelper;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONObject;

public class ResumeScreeningService {

    // DAO and utility class instances
    private UserDao userDao;
    private ResumeDao resumeDao;
    private RecommendationDao recommendationDao;
    private FeedbackDao feedbackDao;
    private SkillDao skillDao;
    private ResumeRanker resumeRanker;
    private SkillService skillService;
    private ResumeScoringService scoringService;

    // Constructor initializing DAO classes and services
    public ResumeScreeningService() {
        this.userDao = new UserDao();
        this.resumeDao = new ResumeDao();
        this.recommendationDao = new RecommendationDao();
        this.feedbackDao = new FeedbackDao();
        this.skillDao = new SkillDao();
        this.resumeRanker = new ResumeRanker();
        this.skillService = new SkillService();
        this.scoringService = new ResumeScoringService();
    }

    // Method to create a new user account
    public void createAccount(Scanner scanner) {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter email: ");
        String email = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        // Save new user to database
        User user = new User(username, email, password);
        userDao.saveUser(user);
        System.out.println("✅ Account created successfully!");
    }

    // Method to upload a resume and perform AI-powered skill extraction
    public void uploadResume(String email, String extractedText) {
        // Call to AI service for skill extraction from resume text
        String aiResponse = AIService.extractSkillsUsingAI(extractedText);
        System.out.println("AI Response: " + aiResponse);

        // Parse the skills from AI response
        List<Skill> extractedSkills = parseSkillsFromAI(aiResponse);

        // Retrieve the existing resume and either create a new one or update it
        Resume resume = resumeDao.getResumeByEmail(email);
        if (resume == null) {
            resume = new Resume(email, "", extractedSkills);
            resume.setExtractedText(extractedText);
            resumeDao.saveResume(resume);
            System.out.println("✅ Resume uploaded successfully with AI-powered skill extraction!");
        } else {
            resume.setExtractedText(extractedText);
            resume.setExtractedSkills(extractedSkills);
            resumeDao.updateResumeDetails(resume.getId(), extractedText, calculateResumeScore(resume));
            System.out.println("✅ Existing resume updated.");
        }
    }

    // Method to upload a resume from Google Drive
    public void uploadResumeFromGoogleDrive(Scanner scanner, String email) {
        System.out.print("Enter Google Drive File ID or URL: ");
        String input = scanner.nextLine();

        // Extract file ID from the input
        String fileId = extractFileId(input);

        if (fileId == null) {
            System.out.println("❌ Invalid Google Drive file ID.");
            return;
        }

        // Download file and extract resume text
        try {
            String filePath = GoogleDriveHelper.downloadFileFromGoogleDrive(fileId);
            String resumeText = resumeDao.extractTextFromResume(filePath);
            if (resumeText == null || resumeText.trim().isEmpty()) {
                System.out.println("❗ Resume text is empty. Upload failed.");
                return;
            }

            System.out.println("Extracted Resume Text: " + resumeText);

            // Call AI service for skill extraction
            String aiResponse = AIService.extractSkillsUsingAI(resumeText);
            System.out.println("AI Response: " + aiResponse);

            // Parse the skills from AI response
            List<Skill> extractedSkills = parseSkillsFromAI(aiResponse);

            // Update or save the resume
            Resume resume = resumeDao.getResumeByEmail(email);
            if (resume == null) {
                resume = new Resume(email, filePath, extractedSkills);
                resume.setExtractedText(resumeText);
                resumeDao.saveResume(resume);
                System.out.println("✅ Resume uploaded successfully with AI-powered skill extraction!");
            } else {
                resume.setExtractedText(resumeText);
                resume.setFilePath(filePath);
                resume.setExtractedSkills(extractedSkills);
                resumeDao.updateResumeDetails(resume.getId(), resumeText, calculateResumeScore(resume));
                System.out.println("✅ Existing resume updated.");
            }
        } catch (IOException e) {
            System.out.println("❌ Error downloading or processing file.");
            e.printStackTrace();
        }
    }

    // Helper method to extract file ID from a Google Drive link or direct ID input
    private String extractFileId(String input) {
        if (input.contains("drive.google.com")) {
            String[] parts = input.split("/d/");
            if (parts.length > 1) {
                return parts[1].split("/")[0];
            }
        } else {
            return input;
        }
        return null;
    }

    // Helper method to parse skills from AI response JSON
    private List<Skill> parseSkillsFromAI(String aiResponse) {
        List<Skill> skillList = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(aiResponse);
            if (jsonObject.has("skills")) {
                JSONArray skillsArray = jsonObject.getJSONArray("skills");
                for (int i = 0; i < skillsArray.length(); i++) {
                    skillList.add(new Skill(skillsArray.getString(i), "Intermediate"));
                }
            } else {
                System.out.println("⚠️ No 'skills' key found in AI response.");
            }
        } catch (Exception e) {
            System.out.println("⚠️ Could not parse skills JSON from AI content.");
            e.printStackTrace();
        }
        return skillList;
    }

    // Method to view screening results for a given email
    public void viewScreeningResult(String email) {
        Resume resume = resumeDao.getResumeByEmail(email);
        if (resume != null) {
            // Calculate resume score
            int score = calculateResumeScore(resume);
            System.out.println("📄 Screening Result for " + email + ":");
            System.out.println("Resume Score: " + score);
            generateRecommendations(resume);  // Generate job recommendations based on resume skills
            getFeedback(email);  // Retrieve and display feedback if available
        } else {
            System.out.println("❌ No resume found for the provided email.");
        }
    }

    // Method to calculate the resume score based on the skills
    private int calculateResumeScore(Resume resume) {
        int score = 0;
        List<String> requiredSkills = Arrays.asList("Java", "Spring", "Hibernate", "MySQL", "Python", "Machine Learning");

        List<Skill> resumeSkills = resume.getExtractedSkills();
        if (resumeSkills != null && !resumeSkills.isEmpty()) {
            for (Skill skill : resumeSkills) {
                if (requiredSkills.contains(skill.getName())) {
                    score += 20;  // Assign 20 points for each required skill found
                }
            }
        } else {
            score = resumeRanker.rankResume(resume);  // Use ranker if no extracted skills
        }

        return Math.min(score, 100);  // Ensure score doesn't exceed 100
    }

    // Method to generate job recommendations based on extracted skills
    public void generateRecommendations(Resume resume) {
        List<Skill> skills = resume.getExtractedSkills();
        if (skills == null || skills.isEmpty()) {
            System.out.println("⚠️ No skills detected. Extracting with AI...");
            skills = AIService.extractSkills(resumeDao.getResumeContentByEmail(resume.getEmail()));
            for (Skill skill : skills) {
                skillDao.saveSkill(skill, resume.getEmail());
            }
        }

        System.out.println("📌 Recommended Jobs:");
        for (Skill skill : skills) {
            System.out.println(" - " + skill.getName() + " Developer");
        }
    }

    // Method to collect and save user feedback for the resume
    public void giveFeedback(Scanner scanner, String email) {
        String resumeText = resumeDao.getResumeContentByEmail(email);
        if (resumeText == null || resumeText.isEmpty()) {
            System.out.println("⚠ No resume found for " + email);
            return;
        }

        // Get feedback from AI based on resume text
        String aiFeedback = AIService.generateFeedback(resumeText);

        System.out.print("Enter additional feedback (or press Enter to skip): ");
        String userFeedback = scanner.nextLine();

        // Combine AI and user feedback
        String finalFeedback = userFeedback.isEmpty() ? (aiFeedback.isEmpty() ? "No feedback available." : aiFeedback) : userFeedback;

        // Get user rating for the feedback
        int rating = getValidRating(scanner);
        Feedback feedback = new Feedback(email, finalFeedback, rating);
        feedbackDao.saveFeedback(feedback);

        System.out.println("✅ Feedback successfully saved for " + email);
    }

    // Method to retrieve and display feedback for a given email
    private void getFeedback(String email) {
        Feedback feedback = feedbackDao.getFeedbackByEmail(email);
        System.out.println("📝 Feedback for " + email + ":");
        if (feedback != null) {
            System.out.println("AI Rating: " + feedback.getRating() + "/5");
            System.out.println("AI Feedback: " + feedback.getFeedbackText());
        } else {
            System.out.println("❌ No feedback available.");
        }
    }

    // Helper method to get a valid rating from the user (1-5)
    public int getValidRating(Scanner scanner) {
        int rating = 0;
        while (rating < 1 || rating > 5) {
            System.out.print("Enter a rating (1 to 5): ");
            try {
                rating = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid input! Please enter a number between 1 and 5.");
            }
        }
        return rating;
    }

    // Helper method to get a valid integer input from the user
    public static int getValidIntegerInput(Scanner scanner) {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("❌ Invalid input! Please enter a valid number (1-5): ");
            }
        }
    }

    // Method to get the file path of a resume based on email
    public String getResumeFilePathByEmail(String email) {
        Resume resume = resumeDao.getResumeByEmail(email);
        if (resume != null) {
            return resume.getFilePath();
        } else {
            System.out.println("⚠️ No resume found for email: " + email);
            return null;
        }
    }
}
