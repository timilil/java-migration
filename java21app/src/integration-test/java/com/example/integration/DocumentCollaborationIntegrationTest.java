package com.example.integration;

import com.example.dto.DocumentDTO;
import com.example.entity.*;
import com.example.repository.*;
import com.example.service.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for document sharing, categorization, and access logging
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {
        IntegrationTestConfig.class
})
@ActiveProfiles("integration-test")
@Transactional
public class DocumentCollaborationIntegrationTest {
    
    @Autowired
    private DocumentRepository documentRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private DocumentShareRepository documentShareRepository;
    
    @Autowired
    private DocumentCategoryRepository documentCategoryRepository;
    
    @Autowired
    private DocumentAccessLogRepository documentAccessLogRepository;
    
    @Autowired
    private DocumentService documentService;
    
    private User ownerUser;
    private User collaboratorUser;
    private Document testDocument;
    
    @BeforeEach
    public void setUp() {
        // Create owner user
        ownerUser = new User();
        ownerUser.setUsername("owner");
        ownerUser.setEmail("owner@example.com");
        ownerUser.setPassword("password123");
        ownerUser = userRepository.save(ownerUser);
        
        // Create collaborator user
        collaboratorUser = new User();
        collaboratorUser.setUsername("collaborator");
        collaboratorUser.setEmail("collaborator@example.com");
        collaboratorUser.setPassword("password123");
        collaboratorUser = userRepository.save(collaboratorUser);
        
        // Create test document
        testDocument = new Document("Shared Project", ownerUser.getId(), "application/pdf");
        testDocument.setDescription("A document to be shared");
        testDocument.setStoragePath("/storage/shared-doc.pdf");
        testDocument = documentRepository.save(testDocument);
    }
    
    // ===== DOCUMENT SHARING TESTS =====
    
    @Test
    public void testShareDocument() {
        boolean shared = documentService.shareDocument(
                testDocument.getId(), 
                ownerUser.getId(), 
                collaboratorUser.getId(), 
                "EDIT"
        );
        
        assertTrue(shared);
        
        List<DocumentShare> shares = documentShareRepository.findByDocumentIdAndActive(testDocument.getId());
        assertNotNull(shares);
        assertTrue(shares.size() > 0);
        assertEquals("EDIT", shares.get(0).getPermissionType());
    }
    
    @Test
    public void testShareDocumentWithViewPermission() {
        documentService.shareDocument(testDocument.getId(), ownerUser.getId(), collaboratorUser.getId(), "VIEW");
        
        Optional<DocumentShare> share = documentShareRepository.findByDocumentIdAndSharedWithUserId(
                testDocument.getId(), 
                collaboratorUser.getId()
        );
        
        assertTrue(share.isPresent());
        assertEquals("VIEW", share.get().getPermissionType());
    }
    
    @Test
    public void testRevokeDocumentShare() {
        // First share the document
        documentService.shareDocument(testDocument.getId(), ownerUser.getId(), collaboratorUser.getId(), "EDIT");
        
        List<DocumentShare> shares = documentShareRepository.findByDocumentIdAndActive(testDocument.getId());
        assertTrue(shares.size() > 0);
        Long shareId = shares.get(0).getId();
        
        // Then revoke the share
        boolean revoked = documentService.revokeDocumentShare(testDocument.getId(), shareId, ownerUser.getId());
        assertTrue(revoked);
        
        // Verify the share is inactive
        Optional<DocumentShare> revokedShare = documentShareRepository.findById(shareId);
        assertTrue(revokedShare.isPresent());
        assertFalse(revokedShare.get().getIsActive());
    }
    
    @Test
    public void testGetDocumentShares() {
        documentService.shareDocument(testDocument.getId(), ownerUser.getId(), collaboratorUser.getId(), "VIEW");
        
        List<DocumentShare> shares = documentService.getDocumentShares(testDocument.getId());
        
        assertNotNull(shares);
        assertTrue(shares.size() > 0);
    }
    
    @Test
    public void testGetSharedDocuments() {
        // Share document with collaborator
        documentService.shareDocument(testDocument.getId(), ownerUser.getId(), collaboratorUser.getId(), "VIEW");
        
        // Get documents shared with collaborator
        List<DocumentDTO> sharedDocs = documentService.getSharedDocuments(collaboratorUser.getId());
        
        assertNotNull(sharedDocs);
        assertTrue(sharedDocs.size() > 0);
    }
    
    @Test
    public void testOwnerHasDocumentAccess() {
        boolean hasAccess = documentService.hasDocumentAccess(testDocument.getId(), ownerUser.getId());
        assertTrue(hasAccess);
    }
    
    @Test
    public void testCollaboratorCanAccessSharedDocument() {
        documentService.shareDocument(testDocument.getId(), ownerUser.getId(), collaboratorUser.getId(), "VIEW");
        
        boolean hasAccess = documentService.hasDocumentAccess(testDocument.getId(), collaboratorUser.getId());
        assertTrue(hasAccess);
    }
    
    @Test
    public void testNoAccessWithoutShare() {
        boolean hasAccess = documentService.hasDocumentAccess(testDocument.getId(), collaboratorUser.getId());
        assertFalse(hasAccess);
    }
    
    // ===== DOCUMENT CATEGORIZATION TESTS =====
    
    @Test
    public void testCreateCategory() {
        DocumentCategory category = documentService.createCategory("Work Documents", ownerUser.getId());
        
        assertNotNull(category.getId());
        assertEquals("Work Documents", category.getName());
        assertEquals(ownerUser.getId(), category.getOwnerId());
    }
    
    @Test
    public void testGetCategories() {
        documentService.createCategory("Work", ownerUser.getId());
        documentService.createCategory("Personal", ownerUser.getId());
        
        List<DocumentCategory> categories = documentService.getCategories(ownerUser.getId());
        
        assertNotNull(categories);
        assertTrue(categories.size() >= 2);
    }
    
    @Test
    public void testCategoryOwnership() {
        DocumentCategory ownerCat = documentService.createCategory("Owner Category", ownerUser.getId());
        DocumentCategory collabCat = documentService.createCategory("Collab Category", collaboratorUser.getId());
        
        List<DocumentCategory> ownerCategories = documentService.getCategories(ownerUser.getId());
        
        assertTrue(ownerCategories.stream().anyMatch(c -> c.getId().equals(ownerCat.getId())));
        assertFalse(ownerCategories.stream().anyMatch(c -> c.getId().equals(collabCat.getId())));
    }
    
    // ===== DOCUMENT ACCESS LOGGING TESTS =====
    
    @Test
    public void testLogDocumentAccess() {
        documentService.logDocumentAccess(testDocument.getId(), ownerUser.getId(), "VIEW", "User viewed the document");
        
        List<DocumentAccessLog> logs = documentService.getDocumentAccessLog(testDocument.getId());
        
        assertNotNull(logs);
        assertTrue(logs.size() > 0);
        assertEquals("VIEW", logs.get(0).getActionType());
    }
    
    @Test
    public void testMultipleAccessLogs() {
        documentService.logDocumentAccess(testDocument.getId(), ownerUser.getId(), "VIEW", "Viewed");
        documentService.logDocumentAccess(testDocument.getId(), ownerUser.getId(), "EDIT", "Edited");
        documentService.logDocumentAccess(testDocument.getId(), collaboratorUser.getId(), "VIEW", "Collaborator viewed");
        
        List<DocumentAccessLog> logs = documentService.getDocumentAccessLog(testDocument.getId());
        
        assertEquals(3, logs.size());
    }
    
    @Test
    public void testGetRecentAccessLogs() {
        documentService.logDocumentAccess(testDocument.getId(), ownerUser.getId(), "VIEW", "Viewed today");
        
        List<DocumentAccessLog> recentLogs = documentService.getRecentAccessLogs(testDocument.getId(), 7);
        
        assertNotNull(recentLogs);
        assertTrue(recentLogs.size() > 0);
    }
    
    @Test
    public void testAccessStatistics() {
        documentService.logDocumentAccess(testDocument.getId(), ownerUser.getId(), "VIEW", "Viewed");
        documentService.logDocumentAccess(testDocument.getId(), ownerUser.getId(), "VIEW", "Viewed again");
        documentService.logDocumentAccess(testDocument.getId(), ownerUser.getId(), "DOWNLOAD", "Downloaded");
        
        String stats = documentService.getAccessStatistics(testDocument.getId());
        
        assertNotNull(stats);
        assertTrue(stats.contains("Views"));
        assertTrue(stats.contains("Downloads"));
    }
    
    @Test
    public void testAccessLogByUser() {
        documentService.logDocumentAccess(testDocument.getId(), ownerUser.getId(), "VIEW", "Owner viewed");
        documentService.logDocumentAccess(testDocument.getId(), collaboratorUser.getId(), "VIEW", "Collaborator viewed");
        
        List<DocumentAccessLog> ownerLogs = documentAccessLogRepository.findByDocumentIdAndUserId(testDocument.getId(), ownerUser.getId());
        List<DocumentAccessLog> collabLogs = documentAccessLogRepository.findByDocumentIdAndUserId(testDocument.getId(), collaboratorUser.getId());
        
        assertEquals(1, ownerLogs.size());
        assertEquals(1, collabLogs.size());
    }
    
    // ===== INTEGRATION SCENARIO TESTS =====
    
    @Test
    public void testCompleteCollaborationScenario() {
        // 1. Owner creates document
        Document newDoc = new Document("Project Proposal", ownerUser.getId(), "application/pdf");
        newDoc = documentRepository.save(newDoc);
        
        // 2. Owner creates category
        DocumentCategory projectsCategory = documentService.createCategory("Projects", ownerUser.getId());
        assertNotNull(projectsCategory.getId());
        
        // 3. Owner shares with collaborator
        boolean shared = documentService.shareDocument(newDoc.getId(), ownerUser.getId(), collaboratorUser.getId(), "EDIT");
        assertTrue(shared);
        
        // 4. Log access by both users
        documentService.logDocumentAccess(newDoc.getId(), ownerUser.getId(), "EDIT", "Owner edited the proposal");
        documentService.logDocumentAccess(newDoc.getId(), collaboratorUser.getId(), "VIEW", "Collaborator reviewed");
        
        // 5. Verify collaborator can see shared document
        boolean collabHasAccess = documentService.hasDocumentAccess(newDoc.getId(), collaboratorUser.getId());
        assertTrue(collabHasAccess);
        
        // 6. Get access log
        // Note: shareDocument() itself logs a "SHARE" event, plus the two explicit logDocumentAccess calls above
        List<DocumentAccessLog> logs = documentService.getDocumentAccessLog(newDoc.getId());
        assertEquals(3, logs.size());
        
        // 7. Verify statistics
        String stats = documentService.getAccessStatistics(newDoc.getId());
        assertTrue(stats.contains("Views"));
        assertTrue(stats.contains("Edits"));
    }
    
    @Test
    public void testAccessControlAfterRevoke() {
        // 1. Share document
        documentService.shareDocument(testDocument.getId(), ownerUser.getId(), collaboratorUser.getId(), "EDIT");
        assertTrue(documentService.hasDocumentAccess(testDocument.getId(), collaboratorUser.getId()));
        
        // 2. Revoke share
        List<DocumentShare> shares = documentShareRepository.findByDocumentIdAndActive(testDocument.getId());
        documentService.revokeDocumentShare(testDocument.getId(), shares.get(0).getId(), ownerUser.getId());
        
        // 3. Verify access is revoked
        assertFalse(documentService.hasDocumentAccess(testDocument.getId(), collaboratorUser.getId()));
    }
}
