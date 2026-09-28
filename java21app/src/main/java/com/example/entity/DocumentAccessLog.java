package com.example.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DocumentAccessLog Entity - audit trail for document access
 * Tracks who accessed which documents and when
 */
@Entity
@Table(name = "document_access_logs", indexes = {
    @Index(name = "idx_access_log_document_id", columnList = "document_id"),
    @Index(name = "idx_access_log_user_id", columnList = "user_id"),
    @Index(name = "idx_access_log_accessed_at", columnList = "accessed_at")
})
public class DocumentAccessLog implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @SequenceGenerator(name = "document_access_log_generator", sequenceName = "document_access_log_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "document_access_log_generator")
    @Column(name = "id")
    private Long id;
    
    @Column(name = "document_id", nullable = false)
    private Long documentId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "action_type", nullable = false, length = 50)
    private String actionType; // VIEW, DOWNLOAD, EDIT, DELETE, SHARE
    
    @Column(name = "accessed_at", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime accessedAt;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    @Column(name = "user_agent", columnDefinition = "VARCHAR(255)")
    private String userAgent;
    @Column(name = "details", columnDefinition = "VARCHAR(255)")
    private String details;
    
    // Constructors
    public DocumentAccessLog() {
        this.accessedAt = LocalDateTime.now();
    }
    
    public DocumentAccessLog(Long documentId, Long userId, String actionType) {
        this();
        this.documentId = documentId;
        this.userId = userId;
        this.actionType = actionType;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    
    public LocalDateTime getAccessedAt() { return accessedAt; }
    public void setAccessedAt(LocalDateTime accessedAt) { this.accessedAt = accessedAt; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}
