package com.ResumeScreening.dao;

import com.ResumeScreening.entity.Resume;
import com.ResumeScreening.util.HibernateUtil;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ResumeDao {

    // ✅ Save a Resume object to the database
    public boolean saveResume(Resume resume) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.save(resume);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
        return false;
    }

    // ✅ Get Resume by Email
    public Resume getResumeByEmail(String email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Resume WHERE email = :email";
            Query<Resume> query = session.createQuery(hql, Resume.class);
            query.setParameter("email", email);
            List<Resume> results = query.list();
            return results.isEmpty() ? null : results.get(0);
        }
    }

    // ✅ Get extracted text by email
    public String getResumeContentByEmail(String email) {
        Resume resume = getResumeByEmail(email);
        if (resume != null) {
            return resume.getExtractedText();
        } else {
            System.out.println("⚠ No extracted text found for email: " + email);
            return null;
        }
    }

    // ✅ Update extracted text and resume rank
    public void updateResumeDetails(Long id, String extractedText, int resumeRank) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Resume resume = session.get(Resume.class, id);
            if (resume != null) {
                resume.setExtractedText(extractedText);
                resume.setRank(resumeRank);
                session.update(resume);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }

    // ✅ Update only resume rank
    public void updateResumeRank(Long id, int resumeRank) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Resume resume = session.get(Resume.class, id);
            if (resume != null) {
                resume.setRank(resumeRank);
                session.update(resume);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }

    // ✅ Public method: Extract text from resume file
    public String extractTextFromResume(String filePath) {
        try {
            File file = new File(filePath);
            Tika tika = new Tika();
            return tika.parseToString(file);
        } catch (IOException | TikaException e) {
            System.out.println("❌ Failed to extract text from resume file: " + filePath);
            e.printStackTrace();
            return "";
        }
    }

    // ✅ Retrieve all resumes
    public List<Resume> getAllResumes() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Resume", Resume.class).list();
        } catch (Exception e) {
            System.err.println("⚠ Error fetching all resumes: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
