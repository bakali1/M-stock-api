package com.mstock.api.entities;

import com.mstock.api.Enum.AuditActionEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * AuditLog Entity - Append-only immutable audit trail
 * Records all changes to entities for compliance and chain of custody
 * Key requirements from CAHIER_DES_CHARGES:
 * - Append-only immutable logs (Section 2.4)
 * - Capture all changes (CREATE/UPDATE/DELETE)
 * - Track WHO, WHAT, WHEN, WHERE (Section 2.4)
 * - Store old/new values as JSON
 */
@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_timestamp", columnList = "timestamp"),
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_action", columnList = "action"),
        @Index(name = "idx_table_name", columnList = "table_name")
})
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditActionEnum action;

    @Column(nullable = false)
    private String tableName;

    @Column(columnDefinition = "TEXT")
    private String oldValue;

    @Column(columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(length = 500)
    private String reason;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant timestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    // IMMUTABLE & APPEND-ONLY: No setters generated
    // This ensures audit logs cannot be modified or deleted after creation
}
