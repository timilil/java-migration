package com.example.repository;

import com.example.entity.DocumentShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * DocumentShare Repository - provides database access for DocumentShare entities
 */
@Repository
public interface DocumentShareRepository extends JpaRepository<DocumentShare, Long> {
    
    /**
     * Find all active shares for a document
     */
    @Query("SELECT s FROM DocumentShare s WHERE s.documentId = :documentId AND s.isActive = true")
    List<DocumentShare> findByDocumentIdAndActive(@Param("documentId") Long documentId);
    
    /**
     * Find documents shared with a specific user
     */
    @Query("SELECT s FROM DocumentShare s WHERE s.sharedWithUserId = :userId AND s.isActive = true")
    List<DocumentShare> findBySharedWithUserIdAndActive(@Param("userId") Long userId);
    
    /**
     * Check if a document is shared with a user
     */
    @Query("SELECT s FROM DocumentShare s WHERE s.documentId = :documentId AND s.sharedWithUserId = :userId AND s.isActive = true")
    Optional<DocumentShare> findByDocumentIdAndSharedWithUserId(@Param("documentId") Long documentId, @Param("userId") Long userId);
    
    /**
     * Find all shares by a user (documents they shared)
     */
    @Query("SELECT s FROM DocumentShare s WHERE s.sharedByUserId = :userId AND s.isActive = true")
    List<DocumentShare> findBySharedByUserIdAndActive(@Param("userId") Long userId);

    /**
     * Find any existing share (active or revoked) for a document/user pair.
     * Used to reactivate a previously revoked share instead of inserting a
     * duplicate row, which would violate the unique (document_id, shared_with_user_id) constraint.
     */
    @Query("SELECT s FROM DocumentShare s WHERE s.documentId = :documentId AND s.sharedWithUserId = :userId")
    Optional<DocumentShare> findByDocumentIdAndSharedWithUserIdIncludingInactive(@Param("documentId") Long documentId, @Param("userId") Long userId);
}
