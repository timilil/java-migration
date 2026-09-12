package com.example.controller;

import com.example.dto.UserDTO;
import com.example.entity.User;
import com.example.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * User REST Controller - provides REST API endpoints for user management
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User Management API")
@Profile("prod")
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    @Autowired
    private UserService userService;
    
    /**
     * Get all users
     * @return list of all users as DTOs
     */
    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieve a list of all users from the database")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        logger.info("GET /api/users - Retrieving all users");
        List<UserDTO> users = userService.getAllUsersAsDTO();
        return ResponseEntity.ok(users);
    }
    
    /**
     * Get user by ID
     * @param id the user ID
     * @return the user DTO if found
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieve a specific user by their ID")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        logger.info("GET /api/users/{} - Retrieving user", id);
        Optional<UserDTO> user = userService.getUserByIdAsDTO(id);
        return user.map(ResponseEntity::ok)
                   .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    /**
     * Get user by username
     * @param username the username
     * @return the user DTO if found
     */
    @GetMapping("/search/username")
    @Operation(summary = "Get user by username", description = "Retrieve a user by their username")
    public ResponseEntity<UserDTO> getUserByUsername(@RequestParam String username) {
        logger.info("GET /api/users/search/username?username={} - Retrieving user", username);
        Optional<UserDTO> user = userService.getUserByUsernameAsDTO(username);
        return user.map(ResponseEntity::ok)
                   .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    /**
     * Create a new user
     * @param user the user data
     * @return the created user DTO
     */
    @PostMapping
    @Operation(summary = "Create user", description = "Create a new user in the database")
    public ResponseEntity<UserDTO> createUser(@RequestBody User user) {
        logger.info("POST /api/users - Creating new user: {}", user.getUsername());
        try {
            UserDTO createdUser = userService.createUserAsDTO(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (IllegalArgumentException e) {
            logger.error("Error creating user: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Update an existing user
     * @param id the user ID
     * @param user the updated user data
     * @return the updated user DTO
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Update an existing user")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody User user) {
        logger.info("PUT /api/users/{} - Updating user", id);
        user.setId(id);
        try {
            UserDTO updatedUser = userService.updateUserAsDTO(user);
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            logger.error("Error updating user: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Delete a user
     * @param id the user ID
     * @return response status
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Delete a user by their ID")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        logger.info("DELETE /api/users/{} - Deleting user", id);
        if (!userService.getUserById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
