package com.example.service;

import com.example.dto.UserDTO;
import com.example.entity.User;
import com.example.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * User Service - Business logic for user management
 */
@Service
@Transactional
@Profile("prod")
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Get all users
     * @return list of all users
     */
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        logger.info("Fetching all users");
        return userRepository.findAll();
    }
    
    /**
     * Get user by ID
     * @param id the user ID
     * @return Optional containing the user if found
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserById(Long id) {
        logger.info("Fetching user with ID: {}", id);
        return userRepository.findById(id);
    }
    
    /**
     * Get user by username
     * @param username the username
     * @return Optional containing the user if found
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserByUsername(String username) {
        logger.info("Fetching user with username: {}", username);
        return userRepository.findByUsername(username);
    }
    
    /**
     * Create a new user
     * @param user the user to create
     * @return the created user
     */
    public User createUser(User user) {
        logger.info("Creating new user with username: {}", user.getUsername());
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + user.getUsername());
        }
        return userRepository.save(user);
    }
    
    /**
     * Update an existing user (password field is optional)
     * @param user the user to update
     * @return the updated user
     */
    public User updateUser(User user) {
        logger.info("Updating user with ID: {}", user.getId());
        Optional<User> existingUser = userRepository.findById(user.getId());
        if (!existingUser.isPresent()) {
            throw new IllegalArgumentException("User not found with ID: " + user.getId());
        }
        User userToUpdate = existingUser.get();
        if (user.getUsername() != null) userToUpdate.setUsername(user.getUsername());
        if (user.getEmail() != null) userToUpdate.setEmail(user.getEmail());
        if (user.getFullName() != null) userToUpdate.setFullName(user.getFullName());
        if (user.getPassword() != null && !user.getPassword().isEmpty()) userToUpdate.setPassword(user.getPassword());
        if (user.getActive() != null) userToUpdate.setActive(user.getActive());
        return userRepository.save(userToUpdate);
    }
    
    /**
     * Delete user by ID
     * @param id the user ID
     */
    public void deleteUser(Long id) {
        logger.info("Deleting user with ID: {}", id);
        userRepository.deleteById(id);
    }
    
    /**
     * Get all users as DTOs (API response format)
     * @return list of user DTOs
     */
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsersAsDTO() {
        logger.info("Fetching all users as DTOs");
        return userRepository.findAll().stream()
                .map(this::entityToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get user by ID as DTO
     * @param id the user ID
     * @return Optional containing the user DTO if found
     */
    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserByIdAsDTO(Long id) {
        logger.info("Fetching user with ID: {} as DTO", id);
        return userRepository.findById(id)
                .map(this::entityToDTO);
    }
    
    /**
     * Get user by username as DTO
     * @param username the username
     * @return Optional containing the user DTO if found
     */
    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserByUsernameAsDTO(String username) {
        logger.info("Fetching user with username: {} as DTO", username);
        return userRepository.findByUsername(username)
                .map(this::entityToDTO);
    }
    
    /**
     * Create a new user and return as DTO
     * @param user the user to create
     * @return the created user DTO
     */
    public UserDTO createUserAsDTO(User user) {
        logger.info("Creating new user with username: {} and returning as DTO", user.getUsername());
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + user.getUsername());
        }
        User saved = userRepository.save(user);
        return entityToDTO(saved);
    }
    
    /**
     * Update an existing user and return as DTO (password field is optional)
     * @param user the user to update
     * @return the updated user DTO
     */
    public UserDTO updateUserAsDTO(User user) {
        logger.info("Updating user with ID: {} and returning as DTO", user.getId());
        Optional<User> existingUser = userRepository.findById(user.getId());
        if (!existingUser.isPresent()) {
            throw new IllegalArgumentException("User not found with ID: " + user.getId());
        }
        User userToUpdate = existingUser.get();
        if (user.getUsername() != null) userToUpdate.setUsername(user.getUsername());
        if (user.getEmail() != null) userToUpdate.setEmail(user.getEmail());
        if (user.getFullName() != null) userToUpdate.setFullName(user.getFullName());
        if (user.getPassword() != null && !user.getPassword().isEmpty()) userToUpdate.setPassword(user.getPassword());
        if (user.getActive() != null) userToUpdate.setActive(user.getActive());
        User updated = userRepository.save(userToUpdate);
        return entityToDTO(updated);
    }
    
    /**
     * Convert User entity to UserDTO
     * @param user the user entity
     * @return the user DTO
     */
    private UserDTO entityToDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
