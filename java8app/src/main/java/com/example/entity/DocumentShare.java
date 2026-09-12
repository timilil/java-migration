package com.example.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DocumentShare Entity - represents document sharing between users
 * Enables collaborative access control to documents
 */
@Entity
@Table(name = "document_shares", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"document_id", "shared_with_user_id"})
})
public class DocumentShare implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "document_id", nullable = false)
    private Long documentId;
    
    @Column(name = "shared_by_user_id", nullable = false)
    private Long sharedByUserId;
    
    @Column(name = "shared_with_user_id", nullable = false)
    private Long sharedWithUserId;
    
    @Column(name = "permission_type", nullable = false, length = 20)
    private String permissionType; // VIEW, EDIT, ADMIN
    
    @Column(name = "shared_at", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime sharedAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(name = "is_active", columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean isActive;
    
    // Constructors
    public DocumentShare() {
        this.sharedAt = LocalDateTime.now();
        this.isActive = true;
        this.permissionType = "VIEW";
    }
    
    public DocumentShare(Long documentId, Long sharedByUserId, Long sharedWithUserId, String permissionType) {
        this();
        this.documentId = documentId;
        this.sharedByUserId = sharedByUserId;
        this.sharedWithUserId = sharedWithUserId;
        this.permissionType = permissionType;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }
    
    public Long getSharedByUserId() { return sharedByUserId; }
    public void setSharedByUserId(Long sharedByUserId) { this.sharedByUserId = sharedByUserId; }
    
    public Long getSharedWithUserId() { return sharedWithUserId; }
    public void setSharedWithUserId(Long sharedWithUserId) { this.sharedWithUserId = sharedWithUserId; }
    
    public String getPermissionType() { return permissionType; }
    public void setPermissionType(String permissionType) { this.permissionType = permissionType; }
    
    public LocalDateTime getSharedAt() { return sharedAt; }
    public void setSharedAt(LocalDateTime sharedAt) { this.sharedAt = sharedAt; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
