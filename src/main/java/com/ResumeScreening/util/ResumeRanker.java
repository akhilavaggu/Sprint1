package com.ResumeScreening.util;

import org.apache.tika.Tika;

import org.apache.tika.exception.TikaException;
import com.ResumeScreening.entity.Resume;
import com.ResumeScreening.dao.ResumeDao;
import com.ResumeScreening.dao.SkillDao;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ResumeRanker {

    private static final List<String> SKILL_KEYWORDS = Arrays.asList(
            "Java", "Python", "Machine Learning", "Deep Learning", "Data Science",
            "SQL", "Hibernate", "Spring Boot", "NLP", "Artificial Intelligence", "C", "C++",
            "Data Analyst", "HTML", "CSS", "JavaScript", "Team Work", "Decision Making", "Communication"
    );

    private ResumeDao resumeDAO = new ResumeDao();
    private SkillDao skillDAO = new SkillDao();

    public static String extractTextFromResume(File resumeFile) {
        Tika tika = new Tika();
        try {
            return tika.parseToString(resumeFile);
        } catch (IOException | TikaException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static List<String> extractSkills(String resumeText) {
        return SKILL_KEYWORDS.stream()
                .filter(resumeText::contains)
                .collect(Collectors.toList());
    }

    public int rankResume(Resume resume) {
        String resumeText = extractTextFromResume(new File(resume.getFilePath()));
        List<String> extractedSkills = extractSkills(resumeText);
        
        // Save extracted skills to database
        skillDAO.saveSkills(resume.getId(), extractedSkills);
        
        // Rank based on the number of matching skills
        int rankScore = extractedSkills.size();
        resumeDAO.updateResumeRank(resume.getId(), rankScore);
        
        return rankScore;
    }
}
