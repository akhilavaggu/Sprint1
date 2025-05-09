package com.ResumeScreening.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "skills")
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String proficiency;

    @ManyToOne
    @JoinColumn(name = "resume_id")
    private Resume resume;

    // ✅ No-argument constructor (required by Hibernate)
    public Skill() {}

    // ✅ Constructor with name & proficiency
    public Skill(String name, String proficiency) {
        this.name = name;
        this.proficiency = proficiency;
    }

    // ✅ Constructor with only name (Default proficiency)
    public Skill(String name) {
        this.name = name;
        this.proficiency = "Intermediate"; // Default proficiency
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }  
    public void setName(String name) { this.name = name; }

    public String getProficiency() { return proficiency; }
    public void setProficiency(String proficiency) { this.proficiency = proficiency; }

    public Resume getResume() { return resume; }
    public void setResume(Resume resume) { this.resume = resume; }
    
 // Overriding equals and hashCode methods for better comparison
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Skill skill = (Skill) obj;
        return id != null && id.equals(skill.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    // ✅ toString() method for debugging
    @Override
    public String toString() {
        return "Skill{id=" + id + ", name='" + name + "', proficiency='" + proficiency + "'}";
    }
}