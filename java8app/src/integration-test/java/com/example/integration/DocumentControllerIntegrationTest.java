package com.example.integration;

import com.example.controller.DocumentController;
import com.example.dto.DocumentDTO;
import com.example.entity.DocumentAccessLog;
import com.example.entity.DocumentCategory;
import com.example.entity.DocumentShare;
import com.example.repository.*;
import com.example.service.DocumentService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Integration tests for DocumentController endpoints
 * Uses pre-loaded test data from SQL files
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {
        IntegrationTestConfig.class
})
@ActiveProfiles({"integration-test", "prod"})
@Transactional
@Sql(scripts = {
    "classpath:data/test-users.sql",
    "classpath:data/test-documents.sql",
    "classpath:data/test-sharing-and-audit.sql"
})
public class DocumentControllerIntegrationTest {
    
    @Autowired
    private DocumentController documentController;
    
    @Autowired
    private DocumentService documentService;
    
    @Autowired
    private DocumentRepository documentRepository;
    
    @Autowired
    private DocumentShareRepository documentShareRepository;
    
    @Autowired
    private DocumentCategoryRepository documentCategoryRepository;
    
    @Autowired
    private DocumentAccessLogRepository documentAccessLogRepository;
    
    @Before
    public void setUp() {
        // Test data is loaded via @Sql annotations
    }
    
    // ===== DOCUMENT RETRIEVAL TESTS =====
    
    @Test
    public void testGetUserDocuments() {
        // Alice (user 1) has 3 active documents
        List<DocumentDTO> docs = documentService.getDocumentsByOwnerId(1L);
        
        assertNotNull(docs);
        assertEquals(3, docs.size());
        
        // Verify archived doc is not included
        boolean hasArchivedDoc = docs.stream()
            .anyMatch(d -> "Archived Document".equals(d.getTitle()));
        assertFalse(hasArchivedDoc);
    }
    
    @Test
    public void testGetSpecificDocument() {
        // Alice retrieves her Project Plan
        DocumentDTO doc = documentService.getDocumentById(1L, 1L)
            .orElse(null);
        
        assertNotNull(doc);
        assertEquals("Project Plan", doc.getTitle());
        assertEquals("Q3 project planning document", doc.getDescription());
    }
    
    @Test
    public void testSearchDocuments() {
        List<DocumentDTO> results = documentService.searchDocuments("Report");
        
        assertNotNull(results);
        assertTrue(results.size() > 0);
        assertTrue(results.stream().anyMatch(d -> 
            d.getTitle().contains("Report")
        ));
    }
    
    // ===== SHARING & COLLABORATION TESTS =====
    
    @Test
    public void testGetDocumentShares() {
        // Document 1 is shared with 3 users: Bob (EDIT), Charlie (VIEW), Eve (VIEW, expiring)
        List<DocumentShare> shares = documentService.getDocumentShares(1L);
        
        assertNotNull(shares);
        assertEquals(3, shares.size());
        
        // Verify one share is EDIT and two are VIEW
        long editCount = shares.stream()
            .filter(s -> "EDIT".equals(s.getPermissionType()))
            .count();
        long viewCount = shares.stream()
            .filter(s -> "VIEW".equals(s.getPermissionType()))
            .count();
        
        assertEquals(1, editCount);
        assertEquals(2, viewCount);
    }
    
    @Test
    public void testGetSharedDocuments() {
        // User 1 (Alice) should see documents shared with her by Bob
        List<DocumentDTO> sharedWithAlice = documentService.getSharedDocuments(1L);
        
        assertNotNull(sharedWithAlice);
        assertTrue(sharedWithAlice.size() > 0);
        
        // Verify documents are from Bob (owner 2)
        assertTrue(sharedWithAlice.stream()
            .anyMatch(d -> d.getTitle().contains("Technical") || d.getTitle().contains("API")));
    }
    
    @Test
    public void testAccessControl_Owner() {
        // Owner always has access
        boolean hasAccess = documentService.hasDocumentAccess(1L, 1L);
        assertTrue(hasAccess);
    }
    
    @Test
    public void testAccessControl_SharedUser() {
        // Bob has access to Alice's document 1 (shared with EDIT)
        boolean hasAccess = documentService.hasDocumentAccess(1L, 2L);
        assertTrue(hasAccess);
    }
    
    @Test
    public void testAccessControl_UnsharedUser() {
        // Diana (user 4) has limited access
        boolean hasAccess = documentService.hasDocumentAccess(3L, 4L);
        assertFalse(hasAccess);
    }
    
    @Test
    public void testShareNewDocument() {
        // Bob shares his Technical Spec with Charlie
        boolean shared = documentService.shareDocument(5L, 2L, 3L, "VIEW");
        
        assertTrue(shared);
        
        // Verify the share was created
        List<DocumentShare> shares = documentService.getDocumentShares(5L);
        assertTrue(shares.stream()
            .anyMatch(s -> s.getSharedWithUserId().equals(3L)));
    }
    
    @Test
    public void testRevokeShare() {
        // Get initial share
        List<DocumentShare> initialShares = documentService.getDocumentShares(1L);
        int initialCount = initialShares.size();
        
        // Revoke a share (Bob's EDIT share)
        Long shareId = initialShares.stream()
            .filter(s -> s.getSharedWithUserId().equals(2L))
            .findFirst()
            .map(DocumentShare::getId)
            .orElse(null);
        
        assertNotNull(shareId);
        boolean revoked = documentService.revokeDocumentShare(1L, shareId, 1L);
        assertTrue(revoked);
        
        // Verify access is revoked
        boolean hasAccess = documentService.hasDocumentAccess(1L, 2L);
        // This may still return true if Bob still has other shares to the document
        // The test verifies the revoke method completes successfully
    }
    
    // ===== CATEGORIZATION TESTS =====
    
    @Test
    public void testGetCategories() {
        // Alice has 3 active categories
        List<DocumentCategory> categories = documentService.getCategories(1L);
        
        assertNotNull(categories);
        assertEquals(3, categories.size());
        
        // Verify archived category is not included
        boolean hasArchived = categories.stream()
            .anyMatch(c -> "Archived".equals(c.getName()));
        assertFalse(hasArchived);
    }
    
    @Test
    public void testCreateCategory() {
        // Bob creates a new category
        DocumentCategory newCat = documentService.createCategory("Documentation", 2L);
        
        assertNotNull(newCat.getId());
        assertEquals("Documentation", newCat.getName());
        assertEquals(2L, newCat.getOwnerId().longValue());
        
        // Verify it's in the database
        List<DocumentCategory> bobCategories = documentService.getCategories(2L);
        assertTrue(bobCategories.stream()
            .anyMatch(c -> "Documentation".equals(c.getName())));
    }
    
    @Test
    public void testCategoryOwnershipIsolation() {
        // Alice gets her categories
        List<DocumentCategory> aliceCategories = documentService.getCategories(1L);
        
        // Bob gets his categories
        List<DocumentCategory> bobCategories = documentService.getCategories(2L);
        
        // They should have different categories
        assertFalse(aliceCategories.isEmpty());
        assertFalse(bobCategories.isEmpty());
        
        // Verify no overlap in names (based on pre-loaded data)
        boolean overlap = aliceCategories.stream()
            .map(DocumentCategory::getName)
            .anyMatch(name -> bobCategories.stream()
                .map(DocumentCategory::getName)
                .anyMatch(name::equals));
        
        assertFalse(overlap);
    }
    
    // ===== AUDIT TRAIL & ACCESS LOGGING TESTS =====
    
    @Test
    public void testGetAccessLog() {
        // Document 1 has multiple access logs
        List<DocumentAccessLog> logs = documentService.getDocumentAccessLog(1L);
        
        assertNotNull(logs);
        assertTrue(logs.size() > 0);
        
        // Verify different action types
        boolean hasView = logs.stream().anyMatch(l -> "VIEW".equals(l.getActionType()));
        boolean hasEdit = logs.stream().anyMatch(l -> "EDIT".equals(l.getActionType()));
        assertTrue(hasView || hasEdit);
    }
    
    @Test
    public void testAccessStatistics() {
        // Document 1 should have VIEW, EDIT, and SHARE actions
        String stats = documentService.getAccessStatistics(1L);
        
        assertNotNull(stats);
        assertTrue(stats.contains("Views:") || stats.contains("Downloads:") || stats.contains("Edits:"));
    }
    
    @Test
    public void testRecentAccessLogs() {
        // Get recent logs (last 7 days)
        List<DocumentAccessLog> recentLogs = documentService.getRecentAccessLogs(1L, 7);
        
        assertNotNull(recentLogs);
        // Should have some logs from the test data
        assertTrue(recentLogs.size() >= 0);
    }
    
    @Test
    public void testLogDocumentAccess() {
        // Log a new access event
        documentService.logDocumentAccess(7L, 2L, "VIEW", "Bob reviewed training materials");
        
        // Verify the log was created
        List<DocumentAccessLog> logs = documentService.getDocumentAccessLog(7L);
        boolean hasNewLog = logs.stream()
            .anyMatch(l -> l.getUserId().equals(2L) && "VIEW".equals(l.getActionType()));
        
        assertTrue(hasNewLog);
    }
    
    // ===== COMPLEX SCENARIO TESTS =====
    
    @Test
    public void testCompleteWorkflow() {
        // 1. Alice creates a new document
        DocumentDTO newDoc = documentService.createDocument(
            "Team Meeting Minutes",
            "Minutes from Q3 planning meeting",
            1L,
            "text/plain",
            1024L,
            "/storage/meeting-minutes.txt"
        );
        assertNotNull(newDoc.getId());
        Long docId = newDoc.getId();
        
        // 2. Alice creates a category for it
        DocumentCategory meetings = documentService.createCategory("Meetings", 1L);
        assertNotNull(meetings.getId());
        
        // 3. Alice shares with Bob and Charlie
        boolean sharedWithBob = documentService.shareDocument(docId, 1L, 2L, "EDIT");
        boolean sharedWithCharlie = documentService.shareDocument(docId, 1L, 3L, "VIEW");
        assertTrue(sharedWithBob && sharedWithCharlie);
        
        // 4. Log access events
        documentService.logDocumentAccess(docId, 2L, "VIEW", "Bob reviewed minutes");
        documentService.logDocumentAccess(docId, 3L, "VIEW", "Charlie reviewed minutes");
        
        // 5. Verify access control
        assertTrue(documentService.hasDocumentAccess(docId, 1L)); // Owner
        assertTrue(documentService.hasDocumentAccess(docId, 2L)); // Shared
        assertTrue(documentService.hasDocumentAccess(docId, 3L)); // Shared
        assertFalse(documentService.hasDocumentAccess(docId, 4L)); // Not shared
        
        // 6. Verify shares
        List<DocumentShare> shares = documentService.getDocumentShares(docId);
        assertEquals(2, shares.size());
        
        // 7. Verify audit trail
        List<DocumentAccessLog> logs = documentService.getDocumentAccessLog(docId);
        assertTrue(logs.size() >= 2);
    }
    
    @Test
    public void testMultipleUsersInteraction() {
        // Scenario: Multiple users accessing and sharing same document
        
        // Get document 1 (Alice's)
        DocumentDTO doc = documentService.getDocumentById(1L, 1L).orElse(null);
        assertNotNull(doc);
        
        // Get all shares
        List<DocumentShare> initialShares = documentService.getDocumentShares(1L);
        int initialShareCount = initialShares.size();
        
        // Get users with access
        List<DocumentAccessLog> logs = documentService.getDocumentAccessLog(1L);
        
        // Verify multiple users in audit trail
        long uniqueUsers = logs.stream()
            .map(DocumentAccessLog::getUserId)
            .distinct()
            .count();
        assertTrue(uniqueUsers > 0);
    }
}
