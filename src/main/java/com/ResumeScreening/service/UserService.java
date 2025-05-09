package com.ResumeScreening.service;

import com.ResumeScreening.entity.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.util.Scanner;

public class UserService {

    private SessionFactory factory;

    // Constructor: Initialize SessionFactory
    public UserService() {
        this.factory = new Configuration().configure("hibernate.cfg.xml")
                                         .addAnnotatedClass(User.class)
                                         .buildSessionFactory();
    }

    // Check if user is already registered
    public boolean isUserRegistered(String email) {
        Session session = factory.getCurrentSession();
        try {
            session.beginTransaction();
            User existingUser = session.createQuery("FROM User WHERE email = :email", User.class)
                                      .setParameter("email", email)
                                      .uniqueResult();
            session.getTransaction().commit();
            return existingUser != null;
        } finally {
            session.close();
        }
    }

    // Get User by Email
    public User getUserByEmail(String email) {
        Session session = factory.getCurrentSession();
        try {
            session.beginTransaction();
            // Query to find user by email
            User user = session.createQuery("FROM User WHERE email = :email", User.class)
                               .setParameter("email", email)
                               .uniqueResult();
            session.getTransaction().commit();
            return user; // Return user if found, else null
        } finally {
            session.close();
        }
    }

    // Create a new user
    public void createAccount(Scanner scanner) {
        System.out.print("Enter your email: ");
        String email = scanner.nextLine().trim();

        // Check if the email is already registered
        if (isUserRegistered(email)) {
            System.out.println("❌ Email already registered. Please log in.");
            return;
        }

        System.out.print("Enter your full name: ");
        String fullName = scanner.nextLine().trim();

        System.out.print("Enter your password: ");
        String password = scanner.nextLine().trim();

        // Create a new user entity
        User newUser = new User(email, fullName, password);

        // Save the user to the database
        Session session = factory.getCurrentSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();
            session.save(newUser);  // Save the user in the database
            transaction.commit();
            System.out.println("✅ Account created successfully! You can now log in.");
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    // Close the factory when done
    public void close() {
        factory.close();
    }
}
