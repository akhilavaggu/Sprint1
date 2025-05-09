package com.ResumeScreening.dao;

import com.ResumeScreening.entity.Recommendation;
import com.ResumeScreening.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class RecommendationDao {

    public void saveRecommendation(Recommendation recommendation) {
        Transaction transaction = null;
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();

            // ✅ Ensure jobDescription is set before saving to avoid null constraint violation
            if (recommendation.getJobDescription() == null || recommendation.getJobDescription().trim().isEmpty()) {
                recommendation.setJobDescription("AI-generated job recommendation based on resume content.");
            }

            // Save the recommendation object to the database
            session.save(recommendation);
            transaction.commit();
            System.out.println("✅ Recommendation saved in DB for: " + recommendation.getEmail());

        } catch (Exception e) {
            if (transaction != null && transaction.getStatus().canRollback()) {
                try {
                    transaction.rollback();
                } catch (Exception rollbackEx) {
                    System.err.println("⚠️ Rollback failed: " + rollbackEx.getMessage());
                }
            }
            e.printStackTrace();
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }
}
