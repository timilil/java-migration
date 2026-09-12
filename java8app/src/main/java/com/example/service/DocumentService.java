package com.example.service;

import com.example.dto.DocumentDTO;
import com.example.entity.Document;
import com.example.entity.DocumentAccessLog;
import com.example.entity.DocumentCategory;
import com.example.entity.DocumentShare;
import com.example.repository.DocumentAccessLogRepository;
import com.example.repository.DocumentCategoryRepository;
import com.example.repository.DocumentRepository;
import com.example.repository.DocumentShareRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Document Service - Business logic for document management including sharing and access control
 */
@Service
@Transactional
@Profile({"prod", "integration-test"})
public class DocumentService {
    
    private static final Logger logger = LoggerFactory.getLogger(DocumentService.class);
    
    @Autowired
    private DocumentRepository documentRepository;
    
    @Autowired
    private DocumentShareRepository documentShareRepository;
    
    @Autowired
    private DocumentCategoryRepository documentCategoryRepository;
    
    @Autowired
    private DocumentAccessLogRepository documentAccessLogRepository;
    
    /**
     * Get all documents for a user
     * @param ownerId the owner's user ID
     * @return list of document DTOs
     */
    @Transactional(readOnly = true)
    public List<DocumentDTO> getDocumentsByOwnerId(Long ownerId) {
        logger.info("Fetching documents for owner ID: {}", ownerId);
        return documentRepository.findByOwnerIdAndActive(ownerId).stream()
                .map(this::entityToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get a specific document by ID
     * @param id the document ID
     * @param ownerId the owner's user ID
     * @return optional containing the document DTO if found
     */
    @Transactional(readOnly = true)
    public Optional<DocumentDTO> getDocumentById(Long id, Long ownerId) {
        logger.info("Fetching document ID: {} for owner ID: {}", id, ownerId);
        return documentRepository.findByIdAndOwnerId(id, ownerId)
                .map(this::entityToDTO);
    }
    
    /**
     * Create a new document
     * @param title the document title
     * @param description the document description
     * @param ownerId the owner's user ID
     * @param contentType the file content type
     * @param fileSize the file size in bytes
     * @param storagePath the storage path for the file
     * @return the created document DTO
     */
    public DocumentDTO createDocument(String title, String description, Long ownerId, 
                                     String contentType, Long fileSize, String storagePath) {
        logger.info("Creating document '{}' for owner ID: {}", title, ownerId);
        
        Document document = new Document(title, ownerId, contentType);
        document.setDescription(description);
        document.setFileSize(fileSize);
        document.setStoragePath(storagePath);
        document.setCreatedAt(LocalDateTime.now());
        document.setUpdatedAt(LocalDateTime.now());
        
        Document saved = documentRepository.save(document);
        logger.info("Document created with ID: {}", saved.getId());
        
        return entityToDTO(saved);
    }
    
    /**
     * Update an existing document
     * @param id the document ID
     * @param ownerId the owner's user ID
     * @param title the new title
     * @param description the new description
     * @return optional containing the updated document DTO
     */
    public Optional<DocumentDTO> updateDocument(Long id, Long ownerId, String title, String description) {
        logger.info("Updating document ID: {} for owner ID: {}", id, ownerId);
        
        Optional<Document> documentOpt = documentRepository.findByIdAndOwnerId(id, ownerId);
        if (documentOpt.isPresent()) {
            Document document = documentOpt.get();
            document.setTitle(title);
            document.setDescription(description);
            document.setUpdatedAt(LocalDateTime.now());
            Document updated = documentRepository.save(document);
            return Optional.of(entityToDTO(updated));
        }
        
        return Optional.empty();
    }
    
    /**
     * Delete a document (soft delete)
     * @param id the document ID
     * @param ownerId the owner's user ID
     * @return true if deleted, false if not found
     */
    public boolean deleteDocument(Long id, Long ownerId) {
        logger.info("Deleting document ID: {} for owner ID: {}", id, ownerId);
        
        Optional<Document> documentOpt = documentRepository.findByIdAndOwnerId(id, ownerId);
        if (documentOpt.isPresent()) {
            Document document = documentOpt.get();
            document.setIsActive(false);
            document.setUpdatedAt(LocalDateTime.now());
            documentRepository.save(document);
            return true;
        }
        
        return false;
    }
    
    /**
     * Search documents by title
     * @param title the search title
     * @return list of matching document DTOs
     */
    @Transactional(readOnly = true)
    public List<DocumentDTO> searchDocuments(String title) {
        logger.info("Searching documents with title: {}", title);
        return documentRepository.findByTitleContainingIgnoreCase(title).stream()
                .map(this::entityToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Convert Document entity to DocumentDTO
     */
    private DocumentDTO entityToDTO(Document document) {
        return new DocumentDTO(
                document.getId(),
                document.getTitle(),
                document.getDescription(),
                document.getOwnerId(),
                document.getContentType(),
                document.getFileSize(),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }
    
    // ===== Document Sharing & Collaboration =====
    
    /**
     * Share a document with another user
     * @param documentId the document ID
     * @param ownerId the owner's user ID
     * @param sharedWithUserId the user to share with
     * @param permissionType the permission level (VIEW, EDIT, ADMIN)
     * @return true if shared successfully
     */
    public boolean shareDocument(Long documentId, Long ownerId, Long sharedWithUserId, String permissionType) {
        logger.info("Sharing document {} with user {} (permission: {})", documentId, sharedWithUserId, permissionType);
        
        // Verify document owner
        Optional<Document> docOpt = documentRepository.findByIdAndOwnerId(documentId, ownerId);
        if (!docOpt.isPresent()) {
            logger.warn("User {} attempted to share document {} they do not own", ownerId, documentId);
            return false;
        }
        
        // Reuse an existing (possibly revoked) share for this document/user pair instead of
        // inserting a new row, since (document_id, shared_with_user_id) is unique
        Optional<DocumentShare> existingShareOpt = documentShareRepository
                .findByDocumentIdAndSharedWithUserIdIncludingInactive(documentId, sharedWithUserId);
        DocumentShare share = existingShareOpt.orElseGet(
                () -> new DocumentShare(documentId, ownerId, sharedWithUserId, permissionType));
        share.setSharedByUserId(ownerId);
        share.setPermissionType(permissionType);
        share.setIsActive(true);
        documentShareRepository.save(share);
        
        // Log access
        logDocumentAccess(documentId, ownerId, "SHARE", "Shared with user " + sharedWithUserId);
        
        return true;
    }
    
    /**
     * Revoke document sharing
     * @param documentId the document ID
     * @param shareId the share ID to revoke
     * @param ownerId the owner's user ID
     * @return true if revoked successfully
     */
    public boolean revokeDocumentShare(Long documentId, Long shareId, Long ownerId) {
        logger.info("Revoking share {} for document {} by owner {}", shareId, documentId, ownerId);
        
        Optional<DocumentShare> shareOpt = documentShareRepository.findById(shareId);
        if (!shareOpt.isPresent() || !shareOpt.get().getDocumentId().equals(documentId) || 
            !shareOpt.get().getSharedByUserId().equals(ownerId)) {
            return false;
        }
        
        DocumentShare share = shareOpt.get();
        share.setIsActive(false);
        documentShareRepository.save(share);
        
        logDocumentAccess(documentId, ownerId, "UNSHARE", "Revoked access for user " + share.getSharedWithUserId());
        
        return true;
    }
    
    /**
     * Get all users a document is shared with
     * @param documentId the document ID
     * @return list of shares
     */
    @Transactional(readOnly = true)
    public List<DocumentShare> getDocumentShares(Long documentId) {
        logger.info("Fetching shares for document {}", documentId);
        return documentShareRepository.findByDocumentIdAndActive(documentId);
    }
    
    /**
     * Get documents shared with a user
     * @param userId the user ID
     * @return list of shared documents
     */
    @Transactional(readOnly = true)
    public List<DocumentDTO> getSharedDocuments(Long userId) {
        logger.info("Fetching documents shared with user {}", userId);
        return documentShareRepository.findBySharedWithUserIdAndActive(userId).stream()
                .map(share -> documentRepository.findById(share.getDocumentId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(this::entityToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Check if user has access to document
     * @param documentId the document ID
     * @param userId the user ID
     * @return true if user has access (owner or shared)
     */
    @Transactional(readOnly = true)
    public boolean hasDocumentAccess(Long documentId, Long userId) {
        Optional<Document> doc = documentRepository.findById(documentId);
        if (!doc.isPresent()) return false;
        
        // Owner always has access
        if (doc.get().getOwnerId().equals(userId)) {
            return true;
        }
        
        // Check if shared with user
        return documentShareRepository.findByDocumentIdAndSharedWithUserId(documentId, userId).isPresent();
    }
    
    // ===== Document Categorization =====
    
    /**
     * Create a document category
     * @param name the category name
     * @param ownerId the owner's user ID
     * @return the created category
     */
    public DocumentCategory createCategory(String name, Long ownerId) {
        logger.info("Creating category '{}' for owner {}", name, ownerId);
        DocumentCategory category = new DocumentCategory(name, ownerId);
        return documentCategoryRepository.save(category);
    }
    
    /**
     * Get all categories for a user
     * @param ownerId the owner's user ID
     * @return list of categories
     */
    @Transactional(readOnly = true)
    public List<DocumentCategory> getCategories(Long ownerId) {
        logger.info("Fetching categories for owner {}", ownerId);
        return documentCategoryRepository.findByOwnerIdAndActive(ownerId);
    }
    
    // ===== Document Access Logging & Audit Trail =====
    
    /**
     * Log document access
     * @param documentId the document ID
     * @param userId the accessing user's ID
     * @param actionType the action type (VIEW, DOWNLOAD, EDIT, DELETE, SHARE)
     * @param details additional details
     */
    public void logDocumentAccess(Long documentId, Long userId, String actionType, String details) {
        DocumentAccessLog log = new DocumentAccessLog(documentId, userId, actionType);
        log.setDetails(details);
        documentAccessLogRepository.save(log);
        logger.debug("Logged document access: document={}, user={}, action={}", documentId, userId, actionType);
    }
    
    /**
     * Get access log for a document
     * @param documentId the document ID
     * @return list of access logs
     */
    @Transactional(readOnly = true)
    public List<DocumentAccessLog> getDocumentAccessLog(Long documentId) {
        logger.info("Fetching access log for document {}", documentId);
        return documentAccessLogRepository.findByDocumentId(documentId);
    }
    
    /**
     * Get recent access logs (last N days)
     * @param documentId the document ID
     * @param days number of days to look back
     * @return list of recent access logs
     */
    @Transactional(readOnly = true)
    public List<DocumentAccessLog> getRecentAccessLogs(Long documentId, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return documentAccessLogRepository.findRecentAccessLogs(documentId, since);
    }
    
    /**
     * Get access statistics for a document
     * @param documentId the document ID
     * @return access statistics as a map-like object
     */
    @Transactional(readOnly = true)
    public String getAccessStatistics(Long documentId) {
        long viewCount = documentAccessLogRepository.countByDocumentIdAndActionType(documentId, "VIEW");
        long downloadCount = documentAccessLogRepository.countByDocumentIdAndActionType(documentId, "DOWNLOAD");
        long editCount = documentAccessLogRepository.countByDocumentIdAndActionType(documentId, "EDIT");
        
        return String.format("Views: %d, Downloads: %d, Edits: %d", viewCount, downloadCount, editCount);
    }
}
