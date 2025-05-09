package com.ResumeScreening.dao;

import com.ResumeScreening.entity.User;
import com.ResumeScreening.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class UserDao {
    public void saveUser(User user) {
        Transaction transaction = null;
        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            
            session.persist(user);  // `persist()` is safer than `save()`
            
            transaction.commit();
            System.out.println("✅ User saved: " + user.getEmail());
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            System.err.println("❌ Error saving user: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (session != null && session.isOpen()) {
                session.close();  // Always close session to prevent memory leaks
            }
        }
    }
}
