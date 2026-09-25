package com.example.repository;

import com.example.entity.DocumentCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * DocumentCategory Repository - provides database access for DocumentCategory entities
 */
@Repository
public interface DocumentCategoryRepository extends JpaRepository<DocumentCategory, Long> {
    
    /**
     * Find all categories owned by a user
     */
    @Query("SELECT c FROM DocumentCategory c WHERE c.ownerId = :ownerId AND c.isActive = true")
    List<DocumentCategory> findByOwnerIdAndActive(@Param("ownerId") Long ownerId);
    
    /**
     * Find a category by ID and owner
     */
    @Query("SELECT c FROM DocumentCategory c WHERE c.id = :id AND c.ownerId = :ownerId AND c.isActive = true")
    Optional<DocumentCategory> findByIdAndOwnerId(@Param("id") Long id, @Param("ownerId") Long ownerId);
    
    /**
     * Find category by name for a user
     */
    @Query("SELECT c FROM DocumentCategory c WHERE LOWER(c.name) = LOWER(:name) AND c.ownerId = :ownerId AND c.isActive = true")
    Optional<DocumentCategory> findByNameAndOwnerId(@Param("name") String name, @Param("ownerId") Long ownerId);
}
