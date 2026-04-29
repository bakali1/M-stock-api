package com.mstock.api.entities;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import com.mstock.api.Enum.TransactionTypeEnum;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Transaction Entity - Immutable after creation
 * Records stock movements (IN|OUT|RETURN)
 * Key requirements from CAHIER_DES_CHARGES:
 * - Record stock movements (RECEIPT|WITHDRAW) (Section 2.3)
 * - Audit trail for chain of custody (Section 2.4)
 * - Timestamp auto-capture
 * - User tracking (who performed action)
 */
@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_batch_id", columnList = "batch_id"),
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_created_at", columnList = "created_at"),
        @Index(name = "idx_product_id", columnList = "product_id")
})
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionTypeEnum type;

    @Column(nullable = false)
    private Integer quantity;

    @Column(length = 500)
    private String reason;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private Batch batch;

    // IMMUTABLE: No setters generated, making this entity read-only after creation
}
