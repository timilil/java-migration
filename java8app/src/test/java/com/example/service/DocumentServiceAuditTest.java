package com.example.service;

import com.example.entity.DocumentAccessLog;
import com.example.repository.DocumentAccessLogRepository;
import com.example.repository.DocumentRepository;
import com.example.repository.DocumentShareRepository;
import com.example.repository.DocumentCategoryRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DocumentService audit logging functionality
 */
@RunWith(MockitoJUnitRunner.class)
public class DocumentServiceAuditTest {
    
    @Mock
    private DocumentAccessLogRepository documentAccessLogRepository;
    
    @Mock
    private DocumentRepository documentRepository;
    
    @Mock
    private DocumentShareRepository documentShareRepository;
    
    @Mock
    private DocumentCategoryRepository documentCategoryRepository;
    
    @InjectMocks
    private DocumentService documentService;
    
    private DocumentAccessLog testLog;
    
    @Before
    public void setUp() {
        testLog = new DocumentAccessLog(1L, 1L, "VIEW");
        testLog.setId(1L);
        testLog.setDetails("User viewed the document");
        testLog.setIpAddress("192.168.1.1");
        testLog.setUserAgent("Mozilla/5.0");
    }
    
    @Test
    public void testLogDocumentAccess() {
        when(documentAccessLogRepository.save(any(DocumentAccessLog.class))).thenReturn(testLog);
        
        // Method returns void, so we just verify it doesn't throw
        documentService.logDocumentAccess(1L, 1L, "VIEW", "User viewed document");
        
        verify(documentAccessLogRepository, times(1)).save(any(DocumentAccessLog.class));
    }
    
    @Test
    public void testLogDocumentAccess_ViewAction() {
        DocumentAccessLog viewLog = new DocumentAccessLog(1L, 1L, "VIEW");
        when(documentAccessLogRepository.save(any(DocumentAccessLog.class))).thenReturn(viewLog);
        
        documentService.logDocumentAccess(1L, 1L, "VIEW", "Viewed document");
        
        verify(documentAccessLogRepository, times(1)).save(any(DocumentAccessLog.class));
    }
    
    @Test
    public void testLogDocumentAccess_EditAction() {
        DocumentAccessLog editLog = new DocumentAccessLog(1L, 1L, "EDIT");
        when(documentAccessLogRepository.save(any(DocumentAccessLog.class))).thenReturn(editLog);
        
        documentService.logDocumentAccess(1L, 1L, "EDIT", "Document edited");
        
        verify(documentAccessLogRepository, times(1)).save(any(DocumentAccessLog.class));
    }
    
    @Test
    public void testLogDocumentAccess_ShareAction() {
        DocumentAccessLog shareLog = new DocumentAccessLog(1L, 1L, "SHARE");
        when(documentAccessLogRepository.save(any(DocumentAccessLog.class))).thenReturn(shareLog);
        
        documentService.logDocumentAccess(1L, 1L, "SHARE", "Shared with user 2");
        
        verify(documentAccessLogRepository, times(1)).save(any(DocumentAccessLog.class));
    }
    
    @Test
    public void testGetDocumentAccessLog() {
        List<DocumentAccessLog> logs = new ArrayList<>();
        logs.add(testLog);
        
        DocumentAccessLog log2 = new DocumentAccessLog(1L, 2L, "EDIT");
        log2.setId(2L);
        log2.setDetails("Another user edited");
        logs.add(log2);
        
        when(documentAccessLogRepository.findByDocumentId(1L)).thenReturn(logs);
        
        List<DocumentAccessLog> result = documentService.getDocumentAccessLog(1L);
        
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("VIEW", result.get(0).getActionType());
        assertEquals("EDIT", result.get(1).getActionType());
        verify(documentAccessLogRepository, times(1)).findByDocumentId(1L);
    }
    
    @Test
    public void testGetDocumentAccessLog_Empty() {
        when(documentAccessLogRepository.findByDocumentId(1L)).thenReturn(new ArrayList<>());
        
        List<DocumentAccessLog> result = documentService.getDocumentAccessLog(1L);
        
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(documentAccessLogRepository, times(1)).findByDocumentId(1L);
    }

    @Test
    public void testGetLatestAccessLogById() {
        DocumentAccessLog latest = new DocumentAccessLog(1L, 1L, "VIEW");
        latest.setId(2L);
        when(documentAccessLogRepository.findFirstByOrderByIdDesc()).thenReturn(latest);

        assertTrue(documentService.getLatestAccessLogById().isPresent());
        assertEquals(Long.valueOf(2L), documentService.getLatestAccessLogById().get().getId());
        verify(documentAccessLogRepository, times(2)).findFirstByOrderByIdDesc();
    }
    
    @Test
    public void testGetRecentAccessLogs() {
        List<DocumentAccessLog> recentLogs = new ArrayList<>();
        recentLogs.add(testLog);
        
        when(documentAccessLogRepository.findRecentAccessLogs(eq(1L), any(LocalDateTime.class))).thenReturn(recentLogs);
        
        List<DocumentAccessLog> result = documentService.getRecentAccessLogs(1L, 7);
        
        assertNotNull(result);
        assertTrue(result.size() >= 0);
        verify(documentAccessLogRepository, times(1)).findRecentAccessLogs(eq(1L), any(LocalDateTime.class));
    }
    
    @Test
    public void testGetRecentAccessLogs_30Days() {
        List<DocumentAccessLog> recentLogs = new ArrayList<>();
        
        when(documentAccessLogRepository.findRecentAccessLogs(eq(1L), any(LocalDateTime.class))).thenReturn(recentLogs);
        
        List<DocumentAccessLog> result = documentService.getRecentAccessLogs(1L, 30);
        
        assertNotNull(result);
        verify(documentAccessLogRepository, times(1)).findRecentAccessLogs(eq(1L), any(LocalDateTime.class));
    }
    
    @Test
    public void testGetAccessStatistics() {
        when(documentAccessLogRepository.countByDocumentIdAndActionType(1L, "VIEW")).thenReturn(5L);
        when(documentAccessLogRepository.countByDocumentIdAndActionType(1L, "DOWNLOAD")).thenReturn(3L);
        when(documentAccessLogRepository.countByDocumentIdAndActionType(1L, "EDIT")).thenReturn(2L);
        
        String result = documentService.getAccessStatistics(1L);
        
        assertNotNull(result);
        assertTrue(result.contains("Views: 5"));
        assertTrue(result.contains("Downloads: 3"));
        assertTrue(result.contains("Edits: 2"));
        verify(documentAccessLogRepository, times(1)).countByDocumentIdAndActionType(1L, "VIEW");
        verify(documentAccessLogRepository, times(1)).countByDocumentIdAndActionType(1L, "DOWNLOAD");
        verify(documentAccessLogRepository, times(1)).countByDocumentIdAndActionType(1L, "EDIT");
    }
    
    @Test
    public void testGetAccessStatistics_NoAccess() {
        when(documentAccessLogRepository.countByDocumentIdAndActionType(1L, "VIEW")).thenReturn(0L);
        when(documentAccessLogRepository.countByDocumentIdAndActionType(1L, "DOWNLOAD")).thenReturn(0L);
        when(documentAccessLogRepository.countByDocumentIdAndActionType(1L, "EDIT")).thenReturn(0L);
        
        String result = documentService.getAccessStatistics(1L);
        
        assertNotNull(result);
        assertTrue(result.contains("Views: 0"));
        assertTrue(result.contains("Downloads: 0"));
        assertTrue(result.contains("Edits: 0"));
    }
    
    @Test
    public void testGetAccessStatistics_HighCounts() {
        when(documentAccessLogRepository.countByDocumentIdAndActionType(1L, "VIEW")).thenReturn(100L);
        when(documentAccessLogRepository.countByDocumentIdAndActionType(1L, "DOWNLOAD")).thenReturn(50L);
        when(documentAccessLogRepository.countByDocumentIdAndActionType(1L, "EDIT")).thenReturn(25L);
        
        String result = documentService.getAccessStatistics(1L);
        
        assertNotNull(result);
        assertTrue(result.contains("Views: 100"));
        assertTrue(result.contains("Downloads: 50"));
        assertTrue(result.contains("Edits: 25"));
    }
    
    @Test
    public void testAccessLogHasTimestamp() {
        DocumentAccessLog log = new DocumentAccessLog(1L, 1L, "VIEW");
        assertNotNull(log.getAccessedAt());
        assertTrue(log.getAccessedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }
    
    @Test
    public void testAccessLogHasActionType() {
        DocumentAccessLog log = new DocumentAccessLog(1L, 1L, "DOWNLOAD");
        assertEquals("DOWNLOAD", log.getActionType());
    }
}
