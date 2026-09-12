package com.example.repository;

import com.example.entity.DocumentAccessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DocumentAccessLog Repository - provides database access for DocumentAccessLog entities
 */
@Repository
public interface DocumentAccessLogRepository extends JpaRepository<DocumentAccessLog, Long> {
    
    /**
     * Find all access logs for a document
     */
    @Query("SELECT l FROM DocumentAccessLog l WHERE l.documentId = :documentId ORDER BY l.accessedAt DESC")
    List<DocumentAccessLog> findByDocumentId(@Param("documentId") Long documentId);
    
    /**
     * Find all access logs for a user
     */
    @Query("SELECT l FROM DocumentAccessLog l WHERE l.userId = :userId ORDER BY l.accessedAt DESC")
    List<DocumentAccessLog> findByUserId(@Param("userId") Long userId);
    
    /**
     * Find access logs for a document by specific user
     */
    @Query("SELECT l FROM DocumentAccessLog l WHERE l.documentId = :documentId AND l.userId = :userId ORDER BY l.accessedAt DESC")
    List<DocumentAccessLog> findByDocumentIdAndUserId(@Param("documentId") Long documentId, @Param("userId") Long userId);
    
    /**
     * Find recent access logs for a document (last N days)
     */
    @Query("SELECT l FROM DocumentAccessLog l WHERE l.documentId = :documentId AND l.accessedAt >= :since ORDER BY l.accessedAt DESC")
    List<DocumentAccessLog> findRecentAccessLogs(@Param("documentId") Long documentId, @Param("since") LocalDateTime since);
    
    /**
     * Count access logs by action type for a document
     */
    @Query("SELECT COUNT(l) FROM DocumentAccessLog l WHERE l.documentId = :documentId AND l.actionType = :actionType")
    long countByDocumentIdAndActionType(@Param("documentId") Long documentId, @Param("actionType") String actionType);
}
