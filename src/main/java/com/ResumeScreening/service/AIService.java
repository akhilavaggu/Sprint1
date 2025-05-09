package com.ResumeScreening.service;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import com.ResumeScreening.entity.Skill;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for handling AI operations like skill extraction,
 * resume rating, feedback generation, and job recommendation using OpenAI's GPT API.
 */
public class AIService {
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String API_KEY = System.getenv("OPENAI_API_KEY");
    private static final OkHttpClient client = new OkHttpClient();

    /**
     * Calls GPT to extract skills from resume text and return them as a raw JSON string.
     */
    public static String extractSkillsUsingAI(String resumeText) {
        JSONObject json = new JSONObject();
        json.put("model", "gpt-4-turbo");
        json.put("messages", new JSONArray()
            .put(new JSONObject().put("role", "system").put("content", "Extract skills from resumes. Output in JSON format with keys: 'skills'."))
            .put(new JSONObject().put("role", "user").put("content", resumeText))
        );
        json.put("max_tokens", 200);
        json.put("temperature", 0.3);

        return sendPostRequest(json);
    }

    /**
     * Generates constructive feedback for a given resume using GPT.
     */
    public static String generateFeedback(String resumeText) {
        JSONObject json = new JSONObject();
        json.put("model", "gpt-4-turbo");
        json.put("messages", new JSONArray()
            .put(new JSONObject().put("role", "system").put("content", "You are a helpful assistant that gives resume feedback."))
            .put(new JSONObject().put("role", "user").put("content", "Analyze this resume and provide constructive feedback:\n" + resumeText))
        );
        json.put("max_tokens", 300);
        json.put("temperature", 0.5);

        String response = sendPostRequest(json);
        try {
            JSONObject responseJson = new JSONObject(response);
            return responseJson.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim();
        } catch (Exception e) {
            System.out.println("⚠️ Error parsing feedback response.");
            e.printStackTrace();
            return "Unable to generate feedback.";
        }
    }

    /**
     * Generates a rating from 1 to 5 for a given resume.
     */
    public static int generateRating(String resumeText) {
        JSONObject json = new JSONObject();
        json.put("model", "gpt-4-turbo");
        json.put("messages", new JSONArray()
            .put(new JSONObject().put("role", "system").put("content", "Rate resumes from 1 to 5. Output only the number."))
            .put(new JSONObject().put("role", "user").put("content", "Rate this resume on a scale of 1 to 5:\n" + resumeText))
        );
        json.put("max_tokens", 10);
        json.put("temperature", 0.3);

        String response = sendPostRequest(json);
        try {
            JSONObject responseJson = new JSONObject(response);
            String ratingText = responseJson.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim();
            return Integer.parseInt(ratingText.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            System.out.println("⚠️ Error parsing rating response.");
            e.printStackTrace();
            return 3; // Default rating if parsing fails
        }
    }

    /**
     * Recommends job titles based on the extracted skills.
     */
    public static String recommendJobs(String skills) {
        JSONObject json = new JSONObject();
        json.put("model", "gpt-4-turbo");
        json.put("messages", new JSONArray()
            .put(new JSONObject().put("role", "system").put("content", "Recommend jobs based on skills. Output JSON format with key 'job_titles'."))
            .put(new JSONObject().put("role", "user").put("content", "Skills: " + skills))
        );
        json.put("max_tokens", 150);
        json.put("temperature", 0.5);

        return sendPostRequest(json);
    }

    /**
     * Common method to send POST requests to the OpenAI API.
     */
    private static String sendPostRequest(JSONObject json) {
        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json"));
        Request request = new Request.Builder()
            .url(API_URL)
            .post(body)
            .addHeader("Authorization", "Bearer " + API_KEY)
            .addHeader("Content-Type", "application/json")
            .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            } else {
                System.out.println("❌ Request failed: " + response.code());
            }
        } catch (IOException e) {
            System.out.println("❌ Network error during AI request.");
            e.printStackTrace();
        }
        return "{}";
    }

    /**
     * Uses GPT to extract a list of skills from the resume text.
     */
    public static List<Skill> extractSkills(String resumeText) {
        JSONObject json = new JSONObject();
        json.put("model", "gpt-4");
        json.put("messages", new JSONArray()
            .put(new JSONObject().put("role", "system").put("content", "Extract only the key technical and soft skills from resumes. Return a JSON with key: 'skills', value as an array of skill names."))
            .put(new JSONObject().put("role", "user").put("content", resumeText))
        );
        json.put("max_tokens", 200);
        json.put("temperature", 0.3);

        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json"));
        Request request = new Request.Builder()
            .url(API_URL)
            .post(body)
            .addHeader("Authorization", "Bearer " + API_KEY)
            .addHeader("Content-Type", "application/json")
            .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseString = response.body().string();
                System.out.println("🧠 Raw AI Response: " + responseString);

                try {
                    JSONObject responseJson = new JSONObject(responseString);
                    JSONObject message = responseJson.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message");
                    String content = message.getString("content").trim();

                    JSONObject contentJson = new JSONObject(content);
                    if (!contentJson.has("skills")) {
                        System.out.println("⚠️ No 'skills' key found in AI response.");
                        return fallbackSkillExtractor(resumeText);
                    }

                    JSONArray skillsArray = contentJson.getJSONArray("skills");
                    List<Skill> skillList = new ArrayList<>();
                    for (int i = 0; i < skillsArray.length(); i++) {
                        skillList.add(new Skill(skillsArray.getString(i), "Intermediate"));
                    }
                    return skillList;
                } catch (Exception e) {
                    System.out.println("⚠️ Could not parse 'skills' JSON.");
                    e.printStackTrace();
                }
            } else {
                System.out.println("❌ AI call failed: " + response.code());
            }
        } catch (IOException e) {
            System.out.println("❌ IOException during skills extraction.");
            e.printStackTrace();
        }

        return fallbackSkillExtractor(resumeText);
    }

    /**
     * Fallback skill extractor in case GPT fails — uses basic keyword matching.
     */
    public static List<Skill> fallbackSkillExtractor(String resumeText) {
        List<String> knownSkills = List.of(
            "C", "Python", "HTML", "CSS", "SQL", "Flutter",
            "Power BI", "Tableau", "Communication", "Adaptability",
            "Teamwork", "Quick Learner", "Time Management", "Leadership"
        );

        List<Skill> matchedSkills = new ArrayList<>();
        for (String skill : knownSkills) {
            if (resumeText.toLowerCase().contains(skill.toLowerCase())) {
                matchedSkills.add(new Skill(skill, "Intermediate"));
            }
        }
        return matchedSkills;
    }
}
