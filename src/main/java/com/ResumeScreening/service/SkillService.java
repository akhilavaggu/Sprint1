package com.ResumeScreening.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import com.ResumeScreening.entity.Skill;

public class SkillService {

    // List of skills to look for in the extracted text (e.g., Java, Python, Machine Learning)
    private static final List<String> SKILL_LIST = Arrays.asList(
        "Java", "Python", "C++", "SQL", "Machine Learning", 
        "Data Science", "Hibernate", "Spring Boot", "AI", "Django"
    );

    /**
     * Extract skills from the given extracted text.
     */
    public List<Skill> extractSkillsFromText(String extractedText) {
        // List to store detected skills
        List<Skill> skills = new ArrayList<>();

        // Loop through each skill in the predefined SKILL_LIST
        for (String skill : SKILL_LIST) {
            // Check if the extracted text contains the skill (case insensitive check)
            if (extractedText.toLowerCase().contains(skill.toLowerCase())) {
                // Add the detected skill with a default proficiency level of "Intermediate"
                skills.add(new Skill(skill, "Intermediate"));
            }
        }

        // Debugging: Print the list of extracted skills for verification
        System.out.println("Extracted Skills: " + skills);

        // Return the list of detected skills
        return skills;
    }
}
