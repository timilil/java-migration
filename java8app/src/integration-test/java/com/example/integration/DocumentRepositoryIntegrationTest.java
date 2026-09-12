package com.example.integration;

import com.example.dto.DocumentDTO;
import com.example.entity.Document;
import com.example.entity.User;
import com.example.repository.DocumentRepository;
import com.example.repository.UserRepository;
import com.example.service.DocumentService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 * Integration tests for Document repository and service
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {
        // Add your test configuration classes here
})
@ActiveProfiles("integration-test")
@Transactional
public class DocumentRepositoryIntegrationTest {
    
    @Autowired
    private DocumentRepository documentRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private DocumentService documentService;
    
    private User testUser;
    private Document testDocument;
    
    @Before
    public void setUp() {
        // Create test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser = userRepository.save(testUser);
        
        // Create test document
        testDocument = new Document("Test Document", testUser.getId(), "application/pdf");
        testDocument.setDescription("A test document");
        testDocument.setFileSize(1024L);
        testDocument.setStoragePath("/storage/test-doc.pdf");
        testDocument = documentRepository.save(testDocument);
    }
    
    @Test
    public void testCreateDocument() {
        Document doc = new Document("New Document", testUser.getId(), "text/plain");
        doc.setDescription("New document description");
        Document saved = documentRepository.save(doc);
        
        assertNotNull(saved.getId());
        assertEquals("New Document", saved.getTitle());
        assertEquals(testUser.getId(), saved.getOwnerId());
    }
    
    @Test
    public void testFindByOwnerIdAndActive() {
        List<Document> docs = documentRepository.findByOwnerIdAndActive(testUser.getId());
        
        assertNotNull(docs);
        assertTrue(docs.size() > 0);
        assertTrue(docs.stream().allMatch(d -> d.getOwnerId().equals(testUser.getId())));
        assertTrue(docs.stream().allMatch(Document::getIsActive));
    }
    
    @Test
    public void testFindByIdAndOwnerId() {
        Optional<Document> found = documentRepository.findByIdAndOwnerId(testDocument.getId(), testUser.getId());
        
        assertTrue(found.isPresent());
        assertEquals(testDocument.getId(), found.get().getId());
        assertEquals("Test Document", found.get().getTitle());
    }
    
    @Test
    public void testFindByIdAndOwnerIdNotFound() {
        Optional<Document> found = documentRepository.findByIdAndOwnerId(999L, testUser.getId());
        
        assertFalse(found.isPresent());
    }
    
    @Test
    public void testFindByTitleContainingIgnoreCase() {
        List<Document> docs = documentRepository.findByTitleContainingIgnoreCase("test");
        
        assertNotNull(docs);
        assertTrue(docs.size() > 0);
        assertTrue(docs.stream().anyMatch(d -> d.getTitle().toLowerCase().contains("test")));
    }
    
    @Test
    public void testCreateDocumentAsDTO() {
        DocumentDTO created = documentService.createDocument(
                "DTO Document",
                "Document created via DTO",
                testUser.getId(),
                "text/html",
                2048L,
                "/storage/dto-doc.html"
        );
        
        assertNotNull(created.getId());
        assertEquals("DTO Document", created.getTitle());
        assertEquals("Document created via DTO", created.getDescription());
        assertEquals(testUser.getId(), created.getOwnerId());
    }
    
    @Test
    public void testGetDocumentByIdAsDTO() {
        Optional<DocumentDTO> found = documentService.getDocumentById(testDocument.getId(), testUser.getId());
        
        assertTrue(found.isPresent());
        assertEquals(testDocument.getTitle(), found.get().getTitle());
        assertEquals(testDocument.getDescription(), found.get().getDescription());
    }
    
    @Test
    public void testUpdateDocument() {
        Optional<DocumentDTO> updated = documentService.updateDocument(
                testDocument.getId(),
                testUser.getId(),
                "Updated Title",
                "Updated description"
        );
        
        assertTrue(updated.isPresent());
        assertEquals("Updated Title", updated.get().getTitle());
        assertEquals("Updated description", updated.get().getDescription());
    }
    
    @Test
    public void testDeleteDocument() {
        boolean deleted = documentService.deleteDocument(testDocument.getId(), testUser.getId());
        assertTrue(deleted);
        
        Optional<Document> found = documentRepository.findByIdAndOwnerId(testDocument.getId(), testUser.getId());
        assertFalse(found.isPresent());
    }
    
    @Test
    public void testGetDocumentsByOwnerId() {
        List<DocumentDTO> docs = documentService.getDocumentsByOwnerId(testUser.getId());
        
        assertNotNull(docs);
        assertTrue(docs.size() > 0);
        assertTrue(docs.stream().allMatch(d -> d.getOwnerId().equals(testUser.getId())));
    }
    
    @Test
    public void testSearchDocuments() {
        List<DocumentDTO> results = documentService.searchDocuments("test");
        
        assertNotNull(results);
        assertTrue(results.stream().anyMatch(d -> d.getTitle().toLowerCase().contains("test")));
    }
}
