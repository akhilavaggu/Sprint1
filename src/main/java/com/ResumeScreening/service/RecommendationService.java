package com.ResumeScreening.service;

import com.ResumeScreening.dao.RecommendationDao;
import com.ResumeScreening.entity.Recommendation;
import com.ResumeScreening.entity.Skill;
import com.ResumeScreening.util.HibernateUtil;

import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class RecommendationService {

    // ✅ Final method to generate and save the recommendation
    public void generateAndSaveRecommendation(String email, List<Skill> skills, String resumeText, Long resumeId) {
        Recommendation recommendation = generateAIRecommendation(email, skills, resumeText, resumeId);
        saveRecommendation(recommendation);
    }

    // ✅ Generates full recommendation with title and description
    public Recommendation generateAIRecommendation(String email, List<Skill> skills, String resumeText, Long resumeId) {
        String jobTitle = "Software Developer";

        if (resumeText.toLowerCase().contains("machine learning") || resumeText.toLowerCase().contains("data science")) {
            jobTitle = "Data Scientist";
        } else if (resumeText.toLowerCase().contains("web development") || skills.toString().toLowerCase().contains("html")) {
            jobTitle = "Frontend Developer";
        } else if (skills.toString().toLowerCase().contains("java") && resumeText.toLowerCase().contains("mysql")) {
            jobTitle = "Java Backend Developer";
        }

        String description = switch (jobTitle) {
            case "Data Scientist" -> "Responsible for analyzing large datasets to extract insights, build predictive models, and support data-driven decision making.";
            case "Frontend Developer" -> "Designs and implements user interfaces using HTML, CSS, and JavaScript frameworks.";
            case "Java Backend Developer" -> "Builds scalable server-side applications using Java, Spring Boot, and database technologies.";
            default -> "Develops, tests, and maintains software applications aligned with client requirements.";
        };

        Recommendation recommendation = new Recommendation();
        recommendation.setEmail(email);
        recommendation.setJobTitle(jobTitle);
        recommendation.setJobDescription(description);
        recommendation.setResumeId(resumeId);
        recommendation.setUserId(1L); // Default or logged-in user ID

        return recommendation;
    }

    // ✅ Final method that saves the recommendation object to DB
    public void saveRecommendation(Recommendation recommendation) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();

        try {
            // Fallback in case description wasn't set
            if (recommendation.getJobDescription() == null || recommendation.getJobDescription().trim().isEmpty()) {
                recommendation.setJobDescription("Job description not provided.");
            }

            // ✅ Log before saving
            System.out.println("Saving Recommendation: " + recommendation);

            session.save(recommendation);
            transaction.commit();

            System.out.println("Recommendation stored successfully!");
        } catch (Exception e) {
            transaction.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }
}
