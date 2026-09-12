package com.example.service;

import com.example.dto.UserDTO;
import com.example.entity.User;
import com.example.repository.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService DTO conversion methods
 */
@RunWith(MockitoJUnitRunner.class)
public class UserServiceDTOTest {
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserService userService;
    
    private User testUser;
    private UserDTO testUserDTO;
    
    @Before
    public void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("alice");
        testUser.setEmail("alice@example.com");
        testUser.setFullName("Alice Anderson");
        testUser.setPassword("password123");
        
        testUserDTO = new UserDTO();
        testUserDTO.setId(1L);
        testUserDTO.setUsername("alice");
        testUserDTO.setEmail("alice@example.com");
        testUserDTO.setFullName("Alice Anderson");
    }
    
    @Test
    public void testGetAllUsersAsDTO() {
        List<User> users = new ArrayList<>();
        users.add(testUser);
        users.add(createSecondUser());
        
        when(userRepository.findAll()).thenReturn(users);
        
        List<UserDTO> result = userService.getAllUsersAsDTO();
        
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("alice", result.get(0).getUsername());
        assertEquals("bob", result.get(1).getUsername());
        verify(userRepository, times(1)).findAll();
    }
    
    @Test
    public void testGetUserByIdAsDTO_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        
        Optional<UserDTO> result = userService.getUserByIdAsDTO(1L);
        
        assertTrue(result.isPresent());
        assertEquals("alice", result.get().getUsername());
        assertEquals("alice@example.com", result.get().getEmail());
        verify(userRepository, times(1)).findById(1L);
    }
    
    @Test
    public void testGetUserByIdAsDTO_NotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        
        Optional<UserDTO> result = userService.getUserByIdAsDTO(999L);
        
        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findById(999L);
    }
    
    @Test
    public void testGetUserByUsernameAsDTO_Success() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(testUser));
        
        Optional<UserDTO> result = userService.getUserByUsernameAsDTO("alice");
        
        assertTrue(result.isPresent());
        assertEquals("alice", result.get().getUsername());
        verify(userRepository, times(1)).findByUsername("alice");
    }
    
    @Test
    public void testGetUserByUsernameAsDTO_NotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        
        Optional<UserDTO> result = userService.getUserByUsernameAsDTO("unknown");
        
        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findByUsername("unknown");
    }
    
    @Test
    public void testCreateUserAsDTO() {
        User newUser = new User();
        newUser.setUsername("charlie");
        newUser.setEmail("charlie@example.com");
        newUser.setFullName("Charlie Clark");
        newUser.setPassword("password123");
        
        User savedUser = new User();
        savedUser.setId(2L);
        savedUser.setUsername("charlie");
        savedUser.setEmail("charlie@example.com");
        savedUser.setFullName("Charlie Clark");
        savedUser.setPassword("password123");
        
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        
        UserDTO result = userService.createUserAsDTO(newUser);
        
        assertNotNull(result);
        assertEquals(2L, result.getId().longValue());
        assertEquals("charlie", result.getUsername());
        verify(userRepository, times(1)).save(any(User.class));
    }
    
    @Test
    public void testUpdateUserAsDTO() {
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setUsername("alice");
        updatedUser.setEmail("newemail@example.com");
        updatedUser.setFullName("Alice Updated");
        updatedUser.setPassword("newpassword");
        
        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(updatedUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        
        UserDTO result = userService.updateUserAsDTO(updatedUser);
        
        assertNotNull(result);
        assertEquals("alice", result.getUsername());
        assertEquals("newemail@example.com", result.getEmail());
        assertEquals("Alice Updated", result.getFullName());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
    }
    
    @Test
    public void testUserDTOExcludesPassword() {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setEmail("alice@example.com");
        user.setFullName("Alice Smith");
        user.setPassword("secretpassword");
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        
        Optional<UserDTO> result = userService.getUserByIdAsDTO(1L);
        
        assertTrue(result.isPresent());
        UserDTO dto = result.get();
        
        // Verify that password field is not included in DTO
        assertEquals("alice", dto.getUsername());
        assertEquals("alice@example.com", dto.getEmail());
        assertFalse(hasPasswordField(dto));
    }
    
    private User createSecondUser() {
        User user = new User();
        user.setId(2L);
        user.setUsername("bob");
        user.setEmail("bob@example.com");
        user.setFullName("Bob Brown");
        user.setPassword("password123");
        return user;
    }
    
    private boolean hasPasswordField(UserDTO dto) {
        try {
            UserDTO.class.getDeclaredField("password");
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }
}
