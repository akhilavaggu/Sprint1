package com.ResumeScreening.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "feedback")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email")
    private String email;

    @Column(name = "feedback_text")
    private String feedbackText;

    @Column(name = "rating")
    private int rating;

    public Feedback() {}

    public Feedback(String email, String feedbackText, int rating) {
        this.email = email;
        this.feedbackText = feedbackText;
        this.rating = rating;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFeedbackText() {
        return feedbackText;
    }

    public void setFeedbackText(String feedbackText) {
        this.feedbackText = feedbackText;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
}
