package com.ResumeScreening.service;

import java.util.List;
import com.ResumeScreening.entity.Skill;

public class ResumeScoringService {
    public int calculateResumeScore(List<Skill> skills) {
        if (skills == null || skills.isEmpty()) {
            System.out.println("No skills detected, resume score = 0");
            return 0;
        }

        int score = 10 * skills.size(); // Example scoring logic
        System.out.println("Calculated Resume Score: " + score);

        return Math.min(score, 100); // Limit score to 100
    }
}
