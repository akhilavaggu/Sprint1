package com.ResumeScreening.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "recommendations")
public class Recommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String jobTitle;

    @Column(nullable = false)
    private String jobDescription;

    @Column(name = "resume_id", nullable = false)
    private Long resumeId;

    private String description;

    private Long userId;

    // Default constructor required by JPA
    public Recommendation() {}

    // Constructor with all required fields
    public Recommendation(String email, String jobTitle, String jobDescription, Long resumeId) {
        this.email = email;
        this.jobTitle = jobTitle;
        this.jobDescription = jobDescription;
        this.resumeId = resumeId;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getJobDescription() { return jobDescription; }
    public void setJobDescription(String jobDescription) { this.jobDescription = jobDescription; }

    public Long getResumeId() { return resumeId; }
    public void setResumeId(Long resumeId) { this.resumeId = resumeId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    @Override
    public String toString() {
        return "==== AI-Based Job Recommendation ====\n" +
               "Job Title: " + jobTitle + "\n" +
               "Description: " + jobDescription + "\n";
    }
}
