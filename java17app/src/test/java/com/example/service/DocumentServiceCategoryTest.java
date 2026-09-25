package com.example.service;

import com.example.entity.DocumentCategory;
import com.example.repository.DocumentCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DocumentService categorization functionality
 */
@ExtendWith(MockitoExtension.class)
public class DocumentServiceCategoryTest {
    
    @Mock
    private DocumentCategoryRepository documentCategoryRepository;
    
    @InjectMocks
    private DocumentService documentService;
    
    private DocumentCategory testCategory;
    
    @BeforeEach
    public void setUp() {
        testCategory = new DocumentCategory("Projects", 1L);
        testCategory.setId(1L);
        testCategory.setDescription("Active project documents");
        testCategory.setColorCode("#FF5733");
    }
    
    @Test
    public void testCreateCategory() {
        when(documentCategoryRepository.save(any(DocumentCategory.class))).thenReturn(testCategory);
        
        DocumentCategory result = documentService.createCategory("Projects", 1L);
        
        assertNotNull(result);
        assertEquals("Projects", result.getName());
        assertEquals(1L, result.getOwnerId().longValue());
        verify(documentCategoryRepository, times(1)).save(any(DocumentCategory.class));
    }
    
    @Test
    public void testCreateCategory_WithDescription() {
        DocumentCategory catWithDesc = new DocumentCategory("Finance", 1L);
        catWithDesc.setId(2L);
        catWithDesc.setDescription("Financial documents");
        
        when(documentCategoryRepository.save(any(DocumentCategory.class))).thenReturn(catWithDesc);
        
        DocumentCategory result = documentService.createCategory("Finance", 1L);
        
        assertNotNull(result);
        assertEquals("Finance", result.getName());
        verify(documentCategoryRepository, times(1)).save(any(DocumentCategory.class));
    }
    
    @Test
    public void testGetCategories_Success() {
        List<DocumentCategory> categories = new ArrayList<>();
        categories.add(testCategory);
        
        DocumentCategory cat2 = new DocumentCategory("Finance", 1L);
        cat2.setId(2L);
        categories.add(cat2);
        
        when(documentCategoryRepository.findByOwnerIdAndActive(1L)).thenReturn(categories);
        
        List<DocumentCategory> result = documentService.getCategories(1L);
        
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Projects", result.get(0).getName());
        assertEquals("Finance", result.get(1).getName());
        verify(documentCategoryRepository, times(1)).findByOwnerIdAndActive(1L);
    }
    
    @Test
    public void testGetCategories_Empty() {
        when(documentCategoryRepository.findByOwnerIdAndActive(1L)).thenReturn(new ArrayList<>());
        
        List<DocumentCategory> result = documentService.getCategories(1L);
        
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(documentCategoryRepository, times(1)).findByOwnerIdAndActive(1L);
    }
    
    @Test
    public void testGetCategories_DifferentOwners() {
        // Alice's categories
        List<DocumentCategory> aliceCategories = new ArrayList<>();
        aliceCategories.add(testCategory);
        
        when(documentCategoryRepository.findByOwnerIdAndActive(1L)).thenReturn(aliceCategories);
        
        // Bob's categories
        DocumentCategory bobCat = new DocumentCategory("Technical", 2L);
        bobCat.setId(3L);
        List<DocumentCategory> bobCategories = new ArrayList<>();
        bobCategories.add(bobCat);
        
        when(documentCategoryRepository.findByOwnerIdAndActive(2L)).thenReturn(bobCategories);
        
        // Alice queries
        List<DocumentCategory> aliceResult = documentService.getCategories(1L);
        assertEquals(1, aliceResult.size());
        assertEquals("Projects", aliceResult.get(0).getName());
        assertEquals(1L, aliceResult.get(0).getOwnerId().longValue());
        
        // Bob queries
        List<DocumentCategory> bobResult = documentService.getCategories(2L);
        assertEquals(1, bobResult.size());
        assertEquals("Technical", bobResult.get(0).getName());
        assertEquals(2L, bobResult.get(0).getOwnerId().longValue());
    }
    
    @Test
    public void testGetCategories_ExcludesInactive() {
        List<DocumentCategory> activeCategories = new ArrayList<>();
        activeCategories.add(testCategory);
        
        // Repository should only return active categories
        when(documentCategoryRepository.findByOwnerIdAndActive(1L)).thenReturn(activeCategories);
        
        List<DocumentCategory> result = documentService.getCategories(1L);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getIsActive());
    }
    
    @Test
    public void testCategoryOwnership() {
        DocumentCategory cat1 = new DocumentCategory("Projects", 1L);
        cat1.setId(1L);
        DocumentCategory cat2 = new DocumentCategory("Finance", 2L);
        cat2.setId(2L);
        
        // User 1 owns cat1 only
        List<DocumentCategory> user1Categories = new ArrayList<>();
        user1Categories.add(cat1);
        when(documentCategoryRepository.findByOwnerIdAndActive(1L)).thenReturn(user1Categories);
        
        // User 2 owns cat2 only
        List<DocumentCategory> user2Categories = new ArrayList<>();
        user2Categories.add(cat2);
        when(documentCategoryRepository.findByOwnerIdAndActive(2L)).thenReturn(user2Categories);
        
        List<DocumentCategory> user1Result = documentService.getCategories(1L);
        List<DocumentCategory> user2Result = documentService.getCategories(2L);
        
        assertEquals(1, user1Result.size());
        assertEquals(1L, user1Result.get(0).getOwnerId().longValue());
        
        assertEquals(1, user2Result.size());
        assertEquals(2L, user2Result.get(0).getOwnerId().longValue());
    }
}
