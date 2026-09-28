package com.example.repository;

import com.example.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Document Repository - provides database access for Document entities
 */
@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    
    /**
     * Find all documents owned by a specific user
     * @param ownerId the owner's user ID
     * @return list of documents owned by the user
     */
    @Query("SELECT d FROM Document d WHERE d.ownerId = :ownerId AND d.isActive = true")
    List<Document> findByOwnerIdAndActive(@Param("ownerId") Long ownerId);
    
    /**
     * Find a document by ID and owner ID
     * @param id the document ID
     * @param ownerId the owner's user ID
     * @return the document if found and owned by the user
     */
    @Query("SELECT d FROM Document d WHERE d.id = :id AND d.ownerId = :ownerId AND d.isActive = true")
    Optional<Document> findByIdAndOwnerId(@Param("id") Long id, @Param("ownerId") Long ownerId);
    
    /**
     * Find documents by title (case-insensitive)
     * @param title the document title
     * @return list of documents matching the title
     */
    @Query("SELECT d FROM Document d WHERE LOWER(d.title) LIKE LOWER(CONCAT('%', :title, '%')) AND d.isActive = true")
    List<Document> findByTitleContainingIgnoreCase(@Param("title") String title);
}
