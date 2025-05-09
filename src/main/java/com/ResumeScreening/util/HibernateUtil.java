package com.ResumeScreening.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {
    private static final SessionFactory sessionFactory = buildSessionFactory();

    // Method to build SessionFactory with error handling
    private static SessionFactory buildSessionFactory() {
        try {
            // Create SessionFactory from hibernate.cfg.xml
            return new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
        } catch (Throwable ex) {
            // Log error and throw exception if session factory creation fails
            System.err.println("SessionFactory creation failed: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    // Method to get SessionFactory instance
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    // Method to shut down the session factory and close all resources
    public static void shutdown() {
        getSessionFactory().close();
    }
}
