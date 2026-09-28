package com.example.integration;

import com.example.entity.User;
import com.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration Test for UserRepository using H2 database
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = IntegrationTestConfig.class)
@ActiveProfiles("integration-test")
@Transactional
public class UserRepositoryIntegrationTest {
    
    @Autowired
    private UserRepository userRepository;
    
    private User testUser;
    
    @BeforeEach
    public void setUp() {
        // Clear repository
        userRepository.deleteAll();

        // Create test data
        testUser = new User("integrationuser", "integration@example.com", "password123");
        testUser.setFullName("Integration Test User");
    }

    @Test
    public void testSaveAndRetrieveUser() {
        // Act
        User savedUser = userRepository.save(testUser);
        Optional<User> retrievedUser = userRepository.findById(savedUser.getId());

        // Assert
        assertTrue(retrievedUser.isPresent());
        assertEquals("integrationuser", retrievedUser.get().getUsername());
        assertEquals("integration@example.com", retrievedUser.get().getEmail());
        assertNotNull(retrievedUser.get().getCreatedAt());
    }

    @Test
    public void testFindByUsername() {
        // Arrange
        userRepository.save(testUser);

        // Act
        Optional<User> foundUser = userRepository.findByUsername("integrationuser");

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals("integration@example.com", foundUser.get().getEmail());
        assertEquals("Integration Test User", foundUser.get().getFullName());
    }

    @Test
    public void testFindByEmail() {
        // Arrange
        userRepository.save(testUser);

        // Act
        Optional<User> foundUser = userRepository.findByEmail("integration@example.com");

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals("integrationuser", foundUser.get().getUsername());
    }

    @Test
    public void testExistsByUsername() {
        // Arrange
        userRepository.save(testUser);

        // Act
        boolean exists = userRepository.existsByUsername("integrationuser");
        boolean notExists = userRepository.existsByUsername("nonexistent");

        // Assert
        assertTrue(exists);
        assertFalse(notExists);
    }

    @Test
    public void testUpdateUser() {
        // Arrange
        User savedUser = userRepository.save(testUser);
        String originalUsername = savedUser.getUsername();

        // Act
        savedUser.setFullName("Updated Full Name");
        savedUser.setActive(false);
        User updatedUser = userRepository.save(savedUser);
        Optional<User> retrievedUser = userRepository.findById(updatedUser.getId());

        // Assert
        assertTrue(retrievedUser.isPresent());
        assertEquals(originalUsername, retrievedUser.get().getUsername());
        assertEquals("Updated Full Name", retrievedUser.get().getFullName());
        assertFalse(retrievedUser.get().getActive());
        assertNotNull(retrievedUser.get().getUpdatedAt());
    }

    @Test
    public void testDeleteUser() {
        // Arrange
        User savedUser = userRepository.save(testUser);
        Long userId = savedUser.getId();

        // Act
        userRepository.deleteById(userId);
        Optional<User> deletedUser = userRepository.findById(userId);

        // Assert
        assertFalse(deletedUser.isPresent());
    }

    @Test
    public void testMultipleUsers() {
        // Arrange
        User user1 = new User("user1", "user1@example.com", "pwd1");
        User user2 = new User("user2", "user2@example.com", "pwd2");
        User user3 = new User("user3", "user3@example.com", "pwd3");

        // Act
        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);
        long count = userRepository.count();

        // Assert
        assertEquals(3, count);
    }
}
