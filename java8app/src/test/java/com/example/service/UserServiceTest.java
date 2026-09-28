package com.example.service;

import com.example.entity.User;
import com.example.repository.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit Test for UserService
 */
public class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserService userService;
    
    private User testUser;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        
        // Create test data
        testUser = new User("testuser", "test@example.com", "password123");
        testUser.setId(1L);
        testUser.setFullName("Test User");
    }
    
    @Test
    public void testGetAllUsers() {
        // Arrange
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findAll()).thenReturn(users);
        
        // Act
        List<User> result = userService.getAllUsers();
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).getUsername());
        verify(userRepository, times(1)).findAll();
    }
    
    @Test
    public void testGetUserById() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        
        // Act
        Optional<User> result = userService.getUserById(1L);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        verify(userRepository, times(1)).findById(1L);
    }
    
    @Test
    public void testGetUserByUsername() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        
        // Act
        Optional<User> result = userService.getUserByUsername("testuser");
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
        verify(userRepository, times(1)).findByUsername("testuser");
    }
    
    @Test
    public void testCreateUser() {
        // Arrange
        User newUser = new User("newuser", "new@example.com", "password456");
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.save(newUser)).thenReturn(newUser);
        
        // Act
        User result = userService.createUser(newUser);
        
        // Assert
        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        verify(userRepository, times(1)).existsByUsername("newuser");
        verify(userRepository, times(1)).save(newUser);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testCreateUserWithDuplicateUsername() {
        // Arrange
        User newUser = new User("testuser", "new@example.com", "password456");
        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        
        // Act & Assert - should throw exception
        userService.createUser(newUser);
    }
    
    @Test
    public void testUpdateUser() {
        // Arrange
        testUser.setFullName("Updated User");
        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(testUser));
        when(userRepository.save(testUser)).thenReturn(testUser);
        
        // Act
        User result = userService.updateUser(testUser);
        
        // Assert
        assertNotNull(result);
        assertEquals("Updated User", result.getFullName());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(testUser);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testUpdateNonExistentUser() {
        // Arrange
        testUser.setId(999L);
        when(userRepository.findById(999L)).thenReturn(java.util.Optional.empty());
        
        // Act & Assert - should throw exception
        userService.updateUser(testUser);
    }
    
    @Test
    public void testDeleteUser() {
        // Arrange
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);
        doNothing().when(userRepository).deleteById(userId);
        
        // Act
        userService.deleteUser(userId);
        
        // Assert
        verify(userRepository, times(1)).deleteById(userId);
    }
}
