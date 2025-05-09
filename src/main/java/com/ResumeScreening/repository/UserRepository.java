package com.ResumeScreening.repository;

import com.ResumeScreening.entity.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class UserRepository {

    // SessionFactory is used to create sessions for interacting with the database.
    private SessionFactory factory;

    /**
     * Constructor initializes the SessionFactory for Hibernate.
     * This factory is used to obtain sessions to interact with the database.
     */
    public UserRepository() {
        // Initialize the factory using the hibernate.cfg.xml configuration file
        // and add the User entity class to it.
        this.factory = new Configuration()
                        .configure("hibernate.cfg.xml")  // This loads the Hibernate configuration file
                        .addAnnotatedClass(User.class)   // Add the User class as a mapped entity
                        .buildSessionFactory();         // Build the SessionFactory
    }

    /**
     * Retrieves a User entity from the database based on the email address.
     */
    public User getUserByEmail(String email) {
        // Get the current session from the SessionFactory
        Session session = factory.getCurrentSession();
        try {
            // Start a transaction
            session.beginTransaction();
            
            // Create a query to retrieve the user by email
            User user = session.createQuery("FROM User WHERE email = :email", User.class)
                               .setParameter("email", email)   // Set the parameter value for the email
                               .uniqueResult();                // Execute the query and get a single result
            
            // Commit the transaction after successfully fetching the data
            session.getTransaction().commit();
            
            // Return the retrieved user object
            return user;
        } finally {
            // Close the session to release resources
            session.close();
        }
    }

    /**
     * Saves a User entity to the database.
     */
    public void saveUser(User user) {
        // Get the current session from the SessionFactory
        Session session = factory.getCurrentSession();
        try {
            // Start a transaction
            session.beginTransaction();
            
            // Save the user to the database
            session.save(user);  // Save the user entity
            
            // Commit the transaction after successfully saving the user
            session.getTransaction().commit();
        } finally {
            // Close the session to release resources
            session.close();
        }
    }
}
