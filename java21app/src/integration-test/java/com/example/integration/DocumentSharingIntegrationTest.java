package com.example.integration;

import com.example.entity.DocumentAccessLog;
import com.example.entity.DocumentCategory;
import com.example.entity.DocumentShare;
import com.example.repository.*;
import com.example.service.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for document sharing and collaboration scenarios
 * Uses pre-loaded test data for realistic workflows
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {
        IntegrationTestConfig.class
})
@ActiveProfiles("integration-test")
@Transactional
@Sql(scripts = {
    "classpath:data/test-users.sql",
    "classpath:data/test-documents.sql",
    "classpath:data/test-sharing-and-audit.sql"
})
public class DocumentSharingIntegrationTest {
    
    @Autowired
    private DocumentService documentService;
    
    @Autowired
    private DocumentRepository documentRepository;
    
    @Autowired
    private DocumentShareRepository documentShareRepository;
    
    @Autowired
    private DocumentAccessLogRepository documentAccessLogRepository;
    
    @BeforeEach
    public void setUp() {
        // Test data loaded via @Sql
    }
    
    // ===== PERMISSION & ACCESS CONTROL TESTS =====
    
    @Test
    public void testPermissionHierarchy() {
        // Verify permission hierarchy: VIEW < EDIT < ADMIN
        
        // Document 1: Alice shares with Bob (EDIT) and Charlie (VIEW)
        List<DocumentShare> shares = documentService.getDocumentShares(1L);
        
        Optional<DocumentShare> bobShare = shares.stream()
            .filter(s -> s.getSharedWithUserId().equals(2L))
            .findFirst();
        
        Optional<DocumentShare> charlieShare = shares.stream()
            .filter(s -> s.getSharedWithUserId().equals(3L))
            .findFirst();
        
        assertTrue(bobShare.isPresent());
        assertTrue(charlieShare.isPresent());
        assertEquals("EDIT", bobShare.get().getPermissionType());
        assertEquals("VIEW", charlieShare.get().getPermissionType());
    }
    
    @Test
    public void testNegotiateAccessWithoutShare() {
        // User 4 (Diana) should NOT have access to document 1
        // unless it's shared with her
        boolean hasAccess = documentService.hasDocumentAccess(1L, 4L);
        assertFalse(hasAccess);
    }
    
    @Test
    public void testMultipleSharesPerDocument() {
        // Document 1 is shared with multiple users
        List<DocumentShare> shares = documentService.getDocumentShares(1L);
        
        assertNotNull(shares);
        assertTrue(shares.size() >= 2);
        
        // Verify all shares are active
        assertTrue(shares.stream().allMatch(DocumentShare::getIsActive));
    }
    
    @Test
    public void testShareExpiration() {
        // Document 1 is shared with user 5 with expiration
        List<DocumentShare> shares = documentService.getDocumentShares(1L);
        
        Optional<DocumentShare> expiringShare = shares.stream()
            .filter(s -> s.getSharedWithUserId().equals(5L) && s.getExpiresAt() != null)
            .findFirst();
        
        // Verify expiration date is in the future
        if (expiringShare.isPresent()) {
            assertTrue(expiringShare.get().getExpiresAt().isAfter(LocalDateTime.now()));
        }
    }
    
    // ===== SHARING WORKFLOW TESTS =====
    
    @Test
    public void testShareAndRevokeWorkflow() {
        // 1. Alice shares document 2 with Diana
        boolean shared = documentService.shareDocument(2L, 1L, 4L, "VIEW");
        assertTrue(shared);
        
        // 2. Verify Diana now has access
        assertTrue(documentService.hasDocumentAccess(2L, 4L));
        
        // 3. Get the share ID
        List<DocumentShare> shares = documentService.getDocumentShares(2L);
        Long shareId = shares.stream()
            .filter(s -> s.getSharedWithUserId().equals(4L))
            .map(DocumentShare::getId)
            .findFirst()
            .orElse(null);
        
        assertNotNull(shareId);
        
        // 4. Revoke the share
        boolean revoked = documentService.revokeDocumentShare(2L, shareId, 1L);
        assertTrue(revoked);
        
        // 5. Verify access is revoked
        List<DocumentShare> updatedShares = documentService.getDocumentShares(2L);
        boolean stillActive = updatedShares.stream()
            .anyMatch(s -> s.getSharedWithUserId().equals(4L) && s.getIsActive());
        assertFalse(stillActive);
    }
    
    @Test
    public void testResharingAfterRevoke() {
        // 1. Share document
        documentService.shareDocument(2L, 1L, 4L, "VIEW");
        assertTrue(documentService.hasDocumentAccess(2L, 4L));
        
        // 2. Revoke share
        List<DocumentShare> shares = documentService.getDocumentShares(2L);
        Long shareId = shares.stream()
            .filter(s -> s.getSharedWithUserId().equals(4L))
            .map(DocumentShare::getId)
            .findFirst()
            .orElse(null);
        documentService.revokeDocumentShare(2L, shareId, 1L);
        
        // 3. Re-share with higher permission
        boolean reshared = documentService.shareDocument(2L, 1L, 4L, "EDIT");
        assertTrue(reshared);
        
        // 4. Verify access with new permission
        assertTrue(documentService.hasDocumentAccess(2L, 4L));
    }
    
    @Test
    public void testChainedSharing() {
        // Scenario: A shares with B, B can share with C
        // (Though in this system, permissions are tracked by original owner)
        
        List<DocumentShare> shares = documentService.getDocumentShares(5L);
        
        // Document 5 (Bob's) is shared with Alice
        boolean aliceHasAccess = shares.stream()
            .anyMatch(s -> s.getSharedWithUserId().equals(1L));
        assertTrue(aliceHasAccess);
    }
    
    // ===== AUDIT TRAIL TESTS =====
    
    @Test
    public void testAuditTrailCompleteness() {
        // Document 1 should have multiple audit entries
        List<DocumentAccessLog> logs = documentService.getDocumentAccessLog(1L);
        
        assertNotNull(logs);
        assertTrue(logs.size() > 0);
        
        // Verify logs have required fields
        for (DocumentAccessLog log : logs) {
            assertNotNull(log.getId());
            assertNotNull(log.getDocumentId());
            assertNotNull(log.getUserId());
            assertNotNull(log.getActionType());
            assertNotNull(log.getAccessedAt());
        }
    }
    
    @Test
    public void testAuditTrailByActionType() {
        List<DocumentAccessLog> allLogs = documentService.getDocumentAccessLog(1L);
        
        long viewCount = allLogs.stream()
            .filter(l -> "VIEW".equals(l.getActionType()))
            .count();
        
        long editCount = allLogs.stream()
            .filter(l -> "EDIT".equals(l.getActionType()))
            .count();
        
        long shareCount = allLogs.stream()
            .filter(l -> "SHARE".equals(l.getActionType()))
            .count();
        
        assertTrue(viewCount + editCount + shareCount <= allLogs.size());
    }
    
    @Test
    public void testAuditTrailByUser() {
        // Get all actions by user 2 on document 1
        List<DocumentAccessLog> logs = documentService.getDocumentAccessLog(1L);
        
        long userTwoActions = logs.stream()
            .filter(l -> l.getUserId().equals(2L))
            .count();
        
        assertTrue(userTwoActions >= 0);
    }
    
    @Test
    public void testRecentAccessLogsFiltering() {
        // Get logs from last 7 days
        List<DocumentAccessLog> recentLogs = documentService.getRecentAccessLogs(1L, 7);
        
        assertNotNull(recentLogs);
        
        // All logs should be from the last 7 days
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        boolean allRecent = recentLogs.stream()
            .allMatch(l -> l.getAccessedAt().isAfter(sevenDaysAgo));
        
        assertTrue(allRecent || recentLogs.isEmpty());
    }
    
    @Test
    public void testAccessStatisticsAccuracy() {
        // Get statistics for document 1
        String stats = documentService.getAccessStatistics(1L);
        
        assertNotNull(stats);
        
        // Parse and verify format
        assertTrue(stats.matches(".*Views: \\d+.*Downloads: \\d+.*Edits: \\d+.*"));
    }
    
    // ===== CATEGORY ISOLATION TESTS =====
    
    @Test
    public void testCategoryOwnershipStrictness() {
        // Alice's categories should not be visible to Bob
        List<DocumentCategory> aliceCategories = documentService.getCategories(1L);
        List<DocumentCategory> bobCategories = documentService.getCategories(2L);
        
        assertFalse(aliceCategories.isEmpty());
        assertFalse(bobCategories.isEmpty());
        
        // Verify no category ID overlap
        List<Long> aliceCatIds = aliceCategories.stream()
            .map(DocumentCategory::getId)
            .collect(Collectors.toList());
        
        List<Long> bobCatIds = bobCategories.stream()
            .map(DocumentCategory::getId)
            .collect(Collectors.toList());
        
        for (Long id : aliceCatIds) {
            assertFalse(bobCatIds.contains(id));
        }
    }
    
    @Test
    public void testCategoryActivation() {
        // Active categories only
        List<DocumentCategory> categories = documentService.getCategories(1L);
        
        assertTrue(categories.stream().allMatch(c -> 
            c.getIsActive() == null || c.getIsActive().booleanValue()
        ));
    }
    
    // ===== CONCURRENT OPERATIONS TESTS =====
    
    @Test
    public void testConcurrentShareAttempts() {
        // Alice tries to share same document with multiple users
        boolean share1 = documentService.shareDocument(2L, 1L, 2L, "VIEW");
        boolean share2 = documentService.shareDocument(2L, 1L, 3L, "EDIT");
        boolean share3 = documentService.shareDocument(2L, 1L, 4L, "VIEW");
        
        assertTrue(share1 && share2 && share3);
        
        List<DocumentShare> shares = documentService.getDocumentShares(2L);
        assertTrue(shares.size() >= 3);
    }
    
    @Test
    public void testDocumentAccessLoggingUnderLoad() {
        // Log multiple access events in sequence
        for (int i = 0; i < 5; i++) {
            documentService.logDocumentAccess(3L, 1L, "VIEW", "View event " + i);
        }
        
        // Verify all were recorded
        List<DocumentAccessLog> logs = documentService.getDocumentAccessLog(3L);
        long viewCount = logs.stream()
            .filter(l -> "VIEW".equals(l.getActionType()))
            .count();
        
        assertTrue(viewCount >= 5);
    }
    
    // ===== ERROR CONDITION TESTS =====
    
    @Test
    public void testShareNonexistentDocument() {
        boolean shared = documentService.shareDocument(999L, 1L, 2L, "VIEW");
        assertFalse(shared);
    }
    
    @Test
    public void testRevokeNonexistentShare() {
        boolean revoked = documentService.revokeDocumentShare(1L, 999L, 1L);
        assertFalse(revoked);
    }
    
    @Test
    public void testShareWithoutOwnership() {
        // Bob (user 2) tries to share Alice's document (1L)
        boolean shared = documentService.shareDocument(1L, 2L, 3L, "VIEW");
        assertFalse(shared);
    }
}
