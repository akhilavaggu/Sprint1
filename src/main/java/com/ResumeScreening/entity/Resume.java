package com.ResumeScreening.entity;

import jakarta.persistence.*;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "resumes")
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;  // Name of the candidate

    @Column(nullable = false, unique = true)
    private String email;  // Unique email

    @Column(nullable = false)
    private String filePath;  // Path of the uploaded file

    @Column(columnDefinition = "TEXT")
    private String extractedText;  // Text extracted from resume (if any)

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Skill> extractedSkills = new ArrayList<>();  // List of skills extracted from the resume

    @Column(name = "resume_rank")
    private Integer rank;  // Rank based on analysis (optional)

    @Lob
    @Column(name = "file_data")
    private byte[] fileData;  // Resume file content

    // Constructors
    public Resume() {}

    public Resume(String email, String filePath, List<Skill> extractedSkills) {
        this.email = email;
        this.filePath = filePath;
        this.extractedSkills = extractedSkills != null ? extractedSkills : new ArrayList<>();
        for (Skill skill : this.extractedSkills) {
            skill.setResume(this);
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getExtractedText() { return extractedText; }
    public void setExtractedText(String extractedText) { this.extractedText = extractedText; }

    public List<Skill> getExtractedSkills() { return extractedSkills; }
    public void setExtractedSkills(List<Skill> extractedSkills) {
        this.extractedSkills = extractedSkills != null ? extractedSkills : new ArrayList<>();
        for (Skill skill : this.extractedSkills) {
            skill.setResume(this);
        }
    }

    public Integer getRank() { return rank; }
    public void setRank(Integer rank) { this.rank = rank; }

    public byte[] getFileData() { return fileData; }
    public void setFileData(byte[] fileData) { this.fileData = fileData; }

    // Overriding equals and hashCode methods for better comparison
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Resume resume = (Resume) obj;
        return id != null && id.equals(resume.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
