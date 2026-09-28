package com.example.service;

import com.example.dto.DocumentDTO;
import com.example.entity.Document;
import com.example.entity.DocumentAccessLog;
import com.example.entity.DocumentShare;
import com.example.repository.DocumentRepository;
import com.example.repository.DocumentShareRepository;
import com.example.repository.DocumentAccessLogRepository;
import com.example.repository.DocumentCategoryRepository;
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
 * Unit tests for DocumentService sharing functionality
 */
@RunWith(MockitoJUnitRunner.class)
public class DocumentServiceSharingTest {
    
    @Mock
    private DocumentRepository documentRepository;
    
    @Mock
    private DocumentShareRepository documentShareRepository;
    
    @Mock
    private DocumentAccessLogRepository documentAccessLogRepository;
    
    @Mock
    private DocumentCategoryRepository documentCategoryRepository;
    
    @InjectMocks
    private DocumentService documentService;
    
    private Document testDocument;
    private DocumentShare testShare;
    
    @Before
    public void setUp() {
        testDocument = new Document("Project Plan", 1L, "application/pdf");
        testDocument.setId(1L);
        testDocument.setStoragePath("/storage/project-plan.pdf");
        
        testShare = new DocumentShare(1L, 1L, 2L, "VIEW");
        testShare.setId(1L);
    }
    
    @Test
    public void testShareDocument_Success() {
        when(documentRepository.findByIdAndOwnerId(1L, 1L)).thenReturn(Optional.of(testDocument));
        when(documentShareRepository.findByDocumentIdAndSharedWithUserIdIncludingInactive(1L, 2L)).thenReturn(Optional.empty());
        when(documentShareRepository.save(any(DocumentShare.class))).thenReturn(testShare);
        
        boolean result = documentService.shareDocument(1L, 1L, 2L, "VIEW");
        
        assertTrue(result);
        verify(documentRepository, times(1)).findByIdAndOwnerId(1L, 1L);
        verify(documentShareRepository, times(1)).save(any(DocumentShare.class));
    }
    
    @Test
    public void testShareDocument_ReactivatesRevokedShare() {
        DocumentShare revokedShare = new DocumentShare(1L, 1L, 2L, "VIEW");
        revokedShare.setId(1L);
        revokedShare.setIsActive(false);
        
        when(documentRepository.findByIdAndOwnerId(1L, 1L)).thenReturn(Optional.of(testDocument));
        when(documentShareRepository.findByDocumentIdAndSharedWithUserIdIncludingInactive(1L, 2L))
                .thenReturn(Optional.of(revokedShare));
        when(documentShareRepository.save(any(DocumentShare.class))).thenReturn(revokedShare);
        
        boolean result = documentService.shareDocument(1L, 1L, 2L, "EDIT");
        
        assertTrue(result);
        assertTrue(revokedShare.getIsActive());
        assertEquals("EDIT", revokedShare.getPermissionType());
        verify(documentShareRepository, times(1)).save(revokedShare);
    }
    
    @Test
    public void testShareDocument_DocumentNotFound() {
        when(documentRepository.findByIdAndOwnerId(999L, 1L)).thenReturn(Optional.empty());
        
        boolean result = documentService.shareDocument(999L, 1L, 2L, "VIEW");
        
        assertFalse(result);
        verify(documentRepository, times(1)).findByIdAndOwnerId(999L, 1L);
        verify(documentShareRepository, never()).save(any(DocumentShare.class));
    }
    
    @Test
    public void testShareDocument_NotOwner() {
        when(documentRepository.findByIdAndOwnerId(1L, 999L)).thenReturn(Optional.empty());
        
        boolean result = documentService.shareDocument(1L, 999L, 2L, "EDIT");
        
        assertFalse(result);
        verify(documentRepository, times(1)).findByIdAndOwnerId(1L, 999L);
        verify(documentShareRepository, never()).save(any(DocumentShare.class));
    }
    
    @Test
    public void testShareDocument_WithEditPermission() {
        DocumentShare editShare = new DocumentShare(1L, 1L, 2L, "EDIT");
        when(documentRepository.findByIdAndOwnerId(1L, 1L)).thenReturn(Optional.of(testDocument));
        when(documentShareRepository.findByDocumentIdAndSharedWithUserIdIncludingInactive(1L, 2L)).thenReturn(Optional.empty());
        when(documentShareRepository.save(any(DocumentShare.class))).thenReturn(editShare);
        
        boolean result = documentService.shareDocument(1L, 1L, 2L, "EDIT");
        
        assertTrue(result);
        verify(documentShareRepository, times(1)).save(any(DocumentShare.class));
    }
    
    @Test
    public void testRevokeDocumentShare_Success() {
        when(documentShareRepository.findById(1L)).thenReturn(Optional.of(testShare));
        when(documentShareRepository.save(any(DocumentShare.class))).thenReturn(testShare);
        
        boolean result = documentService.revokeDocumentShare(1L, 1L, 1L);
        
        assertTrue(result);
        verify(documentShareRepository, times(1)).findById(1L);
        verify(documentShareRepository, times(1)).save(any(DocumentShare.class));
    }
    
    @Test
    public void testRevokeDocumentShare_ShareNotFound() {
        when(documentShareRepository.findById(999L)).thenReturn(Optional.empty());
        
        boolean result = documentService.revokeDocumentShare(1L, 999L, 1L);
        
        assertFalse(result);
        verify(documentShareRepository, times(1)).findById(999L);
        verify(documentShareRepository, never()).save(any(DocumentShare.class));
    }
    
    @Test
    public void testRevokeDocumentShare_WrongOwner() {
        testShare.setSharedByUserId(1L);
        when(documentShareRepository.findById(1L)).thenReturn(Optional.of(testShare));
        
        boolean result = documentService.revokeDocumentShare(1L, 1L, 999L);
        
        assertFalse(result);
        verify(documentShareRepository, times(1)).findById(1L);
        verify(documentShareRepository, never()).save(any(DocumentShare.class));
    }
    
    @Test
    public void testGetDocumentShares() {
        List<DocumentShare> shares = new ArrayList<>();
        shares.add(testShare);
        
        when(documentShareRepository.findByDocumentIdAndActive(1L)).thenReturn(shares);
        
        List<DocumentShare> result = documentService.getDocumentShares(1L);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getSharedWithUserId().longValue());
        verify(documentShareRepository, times(1)).findByDocumentIdAndActive(1L);
    }
    
    @Test
    public void testGetDocumentShares_Empty() {
        when(documentShareRepository.findByDocumentIdAndActive(1L)).thenReturn(new ArrayList<>());
        
        List<DocumentShare> result = documentService.getDocumentShares(1L);
        
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(documentShareRepository, times(1)).findByDocumentIdAndActive(1L);
    }
    
    @Test
    public void testHasDocumentAccess_Owner() {
        when(documentRepository.findById(1L)).thenReturn(Optional.of(testDocument));
        
        boolean result = documentService.hasDocumentAccess(1L, 1L);
        
        assertTrue(result);
        verify(documentRepository, times(1)).findById(1L);
    }
    
    @Test
    public void testHasDocumentAccess_SharedUser() {
        when(documentRepository.findById(1L)).thenReturn(Optional.of(testDocument));
        when(documentShareRepository.findByDocumentIdAndSharedWithUserId(1L, 2L))
            .thenReturn(Optional.of(testShare));
        
        boolean result = documentService.hasDocumentAccess(1L, 2L);
        
        assertTrue(result);
        verify(documentRepository, times(1)).findById(1L);
    }
    
    @Test
    public void testHasDocumentAccess_NoAccess() {
        when(documentRepository.findById(1L)).thenReturn(Optional.of(testDocument));
        when(documentShareRepository.findByDocumentIdAndSharedWithUserId(1L, 3L))
            .thenReturn(Optional.empty());
        
        boolean result = documentService.hasDocumentAccess(1L, 3L);
        
        assertFalse(result);
        verify(documentRepository, times(1)).findById(1L);
    }
    
    @Test
    public void testHasDocumentAccess_DocumentNotFound() {
        when(documentRepository.findById(999L)).thenReturn(Optional.empty());
        
        boolean result = documentService.hasDocumentAccess(999L, 1L);
        
        assertFalse(result);
        verify(documentRepository, times(1)).findById(999L);
    }
}
