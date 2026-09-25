package com.example.controller;

import com.example.dto.DocumentDTO;
import com.example.entity.DocumentAccessLog;
import com.example.entity.DocumentCategory;
import com.example.entity.DocumentShare;
import com.example.service.DocumentService;
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
 * Document REST Controller - provides REST API endpoints for document management
 */
@RestController
@RequestMapping("/api/documents")
@Tag(name = "Documents", description = "Document Management API")
@Profile("prod")
public class DocumentController {
    
    private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);
    
    @Autowired
    private DocumentService documentService;
    
    /**
     * Get all documents for the current user
     * @param ownerId the owner's user ID
     * @return list of document DTOs
     */
    @GetMapping("/user/{ownerId}")
    @Operation(summary = "Get user's documents", description = "Retrieve all documents owned by a specific user")
    public ResponseEntity<List<DocumentDTO>> getUserDocuments(@PathVariable Long ownerId) {
        logger.info("GET /api/documents/user/{} - Retrieving documents for owner", ownerId);
        List<DocumentDTO> documents = documentService.getDocumentsByOwnerId(ownerId);
        return ResponseEntity.ok(documents);
    }
    
    /**
     * Get a specific document by ID
     * @param id the document ID
     * @param ownerId the owner's user ID
     * @return the document DTO if found
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get document by ID", description = "Retrieve a specific document by its ID")
    public ResponseEntity<DocumentDTO> getDocument(@PathVariable Long id, @RequestParam Long ownerId) {
        logger.info("GET /api/documents/{} - Retrieving document", id);
        Optional<DocumentDTO> document = documentService.getDocumentById(id, ownerId);
        return document.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    /**
     * Create a new document
     * @param request the document creation request
     * @return the created document DTO
     */
    @PostMapping
    @Operation(summary = "Create document", description = "Create a new document entry")
    public ResponseEntity<DocumentDTO> createDocument(@RequestBody CreateDocumentRequest request) {
        logger.info("POST /api/documents - Creating new document: {}", request.getTitle());
        DocumentDTO created = documentService.createDocument(
                request.getTitle(),
                request.getDescription(),
                request.getOwnerId(),
                request.getContentType(),
                request.getFileSize(),
                request.getStoragePath()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    /**
     * Update an existing document
     * @param id the document ID
     * @param request the update request
     * @return the updated document DTO
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update document", description = "Update an existing document")
    public ResponseEntity<DocumentDTO> updateDocument(@PathVariable Long id, @RequestBody UpdateDocumentRequest request) {
        logger.info("PUT /api/documents/{} - Updating document", id);
        Optional<DocumentDTO> updated = documentService.updateDocument(id, request.getOwnerId(), request.getTitle(), request.getDescription());
        return updated.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    /**
     * Delete a document
     * @param id the document ID
     * @param ownerId the owner's user ID
     * @return 204 No Content if deleted, 404 if not found
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete document", description = "Delete a document (soft delete)")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id, @RequestParam Long ownerId) {
        logger.info("DELETE /api/documents/{} - Deleting document", id);
        boolean deleted = documentService.deleteDocument(id, ownerId);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
    
    /**
     * Search documents by title
     * @param title the search query
     * @return list of matching document DTOs
     */
    @GetMapping("/search")
    @Operation(summary = "Search documents", description = "Search for documents by title")
    public ResponseEntity<List<DocumentDTO>> searchDocuments(@RequestParam String title) {
        logger.info("GET /api/documents/search?title={} - Searching documents", title);
        List<DocumentDTO> results = documentService.searchDocuments(title);
        return ResponseEntity.ok(results);
    }
    
    // ===== SHARING & COLLABORATION ENDPOINTS =====
    
    /**
     * Share a document with another user
     */
    @PostMapping("/{id}/share")
    @Operation(summary = "Share document", description = "Share a document with another user")
    public ResponseEntity<String> shareDocument(@PathVariable Long id, @RequestBody ShareDocumentRequest request) {
        logger.info("POST /api/documents/{}/share - Sharing with user {}", id, request.getSharedWithUserId());
        boolean success = documentService.shareDocument(id, request.getOwnerId(), request.getSharedWithUserId(), request.getPermissionType());
        return success ? ResponseEntity.ok("Document shared successfully") : ResponseEntity.status(HttpStatus.FORBIDDEN).body("Failed to share document");
    }
    
    /**
     * Revoke document sharing
     */
    @DeleteMapping("/{id}/share/{shareId}")
    @Operation(summary = "Revoke share", description = "Revoke a document share")
    public ResponseEntity<Void> revokeShare(@PathVariable Long id, @PathVariable Long shareId, @RequestParam Long ownerId) {
        logger.info("DELETE /api/documents/{}/share/{} - Revoking share", id, shareId);
        boolean success = documentService.revokeDocumentShare(id, shareId, ownerId);
        return success ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
    
    /**
     * Get document shares
     */
    @GetMapping("/{id}/shares")
    @Operation(summary = "Get document shares", description = "Get all users a document is shared with")
    public ResponseEntity<List<DocumentShare>> getDocumentShares(@PathVariable Long id) {
        logger.info("GET /api/documents/{}/shares - Getting document shares", id);
        List<DocumentShare> shares = documentService.getDocumentShares(id);
        return ResponseEntity.ok(shares);
    }
    
    /**
     * Get documents shared with current user
     */
    @GetMapping("/shared-with/{userId}")
    @Operation(summary = "Get shared documents", description = "Get all documents shared with the current user")
    public ResponseEntity<List<DocumentDTO>> getSharedDocuments(@PathVariable Long userId) {
        logger.info("GET /api/documents/shared-with/{} - Getting shared documents", userId);
        List<DocumentDTO> sharedDocs = documentService.getSharedDocuments(userId);
        return ResponseEntity.ok(sharedDocs);
    }
    
    /**
     * Check if user has access to document
     */
    @GetMapping("/{id}/access")
    @Operation(summary = "Check access", description = "Check if user has access to a document")
    public ResponseEntity<Boolean> checkDocumentAccess(@PathVariable Long id, @RequestParam Long userId) {
        logger.info("GET /api/documents/{}/access - Checking access for user {}", id, userId);
        boolean hasAccess = documentService.hasDocumentAccess(id, userId);
        return ResponseEntity.ok(hasAccess);
    }
    
    // ===== CATEGORIZATION ENDPOINTS =====
    
    /**
     * Create document category
     */
    @PostMapping("/categories")
    @Operation(summary = "Create category", description = "Create a new document category")
    public ResponseEntity<DocumentCategory> createCategory(@RequestBody CreateCategoryRequest request) {
        logger.info("POST /api/documents/categories - Creating category: {}", request.getName());
        DocumentCategory category = documentService.createCategory(request.getName(), request.getOwnerId());
        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }
    
    /**
     * Get user's categories
     */
    @GetMapping("/categories/{ownerId}")
    @Operation(summary = "Get categories", description = "Get all categories for a user")
    public ResponseEntity<List<DocumentCategory>> getCategories(@PathVariable Long ownerId) {
        logger.info("GET /api/documents/categories/{} - Getting categories", ownerId);
        List<DocumentCategory> categories = documentService.getCategories(ownerId);
        return ResponseEntity.ok(categories);
    }
    
    // ===== AUDIT & ACCESS LOGGING ENDPOINTS =====
    
    /**
     * Get access log for a document
     */
    @GetMapping("/{id}/access-log")
    @Operation(summary = "Get access log", description = "Get access audit log for a document")
    public ResponseEntity<List<DocumentAccessLog>> getAccessLog(@PathVariable Long id) {
        logger.info("GET /api/documents/{}/access-log - Getting access log", id);
        List<DocumentAccessLog> logs = documentService.getDocumentAccessLog(id);
        return ResponseEntity.ok(logs);
    }
    
    /**
     * Get recent access logs
     */
    @GetMapping("/{id}/access-log/recent")
    @Operation(summary = "Get recent access logs", description = "Get recent access logs for a document")
    public ResponseEntity<List<DocumentAccessLog>> getRecentAccessLogs(@PathVariable Long id, @RequestParam(defaultValue = "7") int days) {
        logger.info("GET /api/documents/{}/access-log/recent - Getting recent logs (last {} days)", id, days);
        List<DocumentAccessLog> logs = documentService.getRecentAccessLogs(id, days);
        return ResponseEntity.ok(logs);
    }
    
    /**
     * Get access statistics
     */
    @GetMapping("/{id}/access-stats")
    @Operation(summary = "Get access statistics", description = "Get access statistics for a document")
    public ResponseEntity<String> getAccessStatistics(@PathVariable Long id) {
        logger.info("GET /api/documents/{}/access-stats - Getting access statistics", id);
        String stats = documentService.getAccessStatistics(id);
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Request class for creating documents
     */
    public static class CreateDocumentRequest {
        private String title;
        private String description;
        private Long ownerId;
        private String contentType;
        private Long fileSize;
        private String storagePath;
        
        public CreateDocumentRequest() {}
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public Long getOwnerId() { return ownerId; }
        public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
        
        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }
        
        public Long getFileSize() { return fileSize; }
        public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
        
        public String getStoragePath() { return storagePath; }
        public void setStoragePath(String storagePath) { this.storagePath = storagePath; }
    }
    
    /**
     * Request class for updating documents
     */
    public static class UpdateDocumentRequest {
        private String title;
        private String description;
        private Long ownerId;
        
        public UpdateDocumentRequest() {}
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public Long getOwnerId() { return ownerId; }
        public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    }
    
    /**
     * Request class for sharing documents
     */
    public static class ShareDocumentRequest {
        private Long ownerId;
        private Long sharedWithUserId;
        private String permissionType; // VIEW, EDIT, ADMIN
        
        public ShareDocumentRequest() {}
        
        public Long getOwnerId() { return ownerId; }
        public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
        
        public Long getSharedWithUserId() { return sharedWithUserId; }
        public void setSharedWithUserId(Long sharedWithUserId) { this.sharedWithUserId = sharedWithUserId; }
        
        public String getPermissionType() { return permissionType; }
        public void setPermissionType(String permissionType) { this.permissionType = permissionType; }
    }
    
    /**
     * Request class for creating categories
     */
    public static class CreateCategoryRequest {
        private String name;
        private String description;
        private Long ownerId;
        
        public CreateCategoryRequest() {}
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public Long getOwnerId() { return ownerId; }
        public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    }
}
