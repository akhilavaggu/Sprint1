package com.ResumeScreening.dao;

import com.ResumeScreening.entity.Skill;
import com.ResumeScreening.entity.Resume;
import com.ResumeScreening.util.HibernateUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class SkillDao {

	public void saveSkill(Skill skill, String email) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Resume resume = session.createQuery("FROM Resume WHERE email = :email", Resume.class)
                                   .setParameter("email", email)
                                   .uniqueResult();

            if (resume != null) {
                skill.setResume(resume);  // Associate skill with resume
                session.persist(skill);
            } else {
                System.out.println("No resume found for email: " + email);
            }

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }



    public void saveSkills(Long resumeId, List<String> skills) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Resume resume = session.get(Resume.class, resumeId);
            if (resume != null) {
                for (String skillName : skills) {
                    Skill skill = new Skill(skillName);
                    skill.setResume(resume);
                    session.save(skill);
                }
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }

    public List<Skill> getSkillsByEmail(String email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Skill WHERE resume.email = :email", Skill.class)
                    .setParameter("email", email)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
