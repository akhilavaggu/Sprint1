package com.ResumeScreening.dao;

import com.ResumeScreening.entity.Feedback;
import java.sql.*;

public class FeedbackDao {
    private static final String URL = "jdbc:mysql://localhost:3306/resume_screening_db";
    private static final String USER = "root";
    private static final String PASSWORD = "12345678";

    public void saveFeedback(Feedback feedback) {
        String query = "INSERT INTO feedback (email, feedback_text, rating) VALUES (?, ?, ?)";

        try (
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            PreparedStatement stmt = conn.prepareStatement(query)
        ) {
            stmt.setString(1, feedback.getEmail());
            stmt.setString(2, feedback.getFeedbackText());
            stmt.setInt(3, feedback.getRating());

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("✅ Feedback successfully saved for " + feedback.getEmail());
            } else {
                System.out.println("⚠️ Failed to save feedback.");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error saving feedback: " + e.getMessage());
        }
    }

    public Feedback getFeedbackByEmail(String email) {
        String query = "SELECT * FROM feedback WHERE email = ?";

        try (
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            PreparedStatement stmt = conn.prepareStatement(query)
        ) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Feedback(
                    rs.getString("email"),
                    rs.getString("feedback_text"),
                    rs.getInt("rating")
                );
            }
        } catch (SQLException e) {
            System.err.println("❌ Error retrieving feedback: " + e.getMessage());
        }

        return null;
    }
}
