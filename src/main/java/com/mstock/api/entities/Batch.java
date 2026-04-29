package com.mstock.api.entities;

import com.mstock.api.Enum.BatchStatusEnum;
import com.mstock.api.payload.Request.BatchRequest;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Batch Entity - Represents a lot/batch of products
 * Key requirements from CAHIER_DES_CHARGES:
 * - Lot tracking with expiration dates (Section 2.1)
 * - Location tracking (bin) for traceability (Section 2.2)
 * - Status management (ACTIVE|QUARANTINE|RETIRED) (Section 2.2)
 */
@Entity
@Table(name = "batches", indexes = {
        @Index(name = "idx_lot_number", columnList = "lot_number"),
        @Index(name = "idx_expiration_date", columnList = "expiration_date"),
        @Index(name = "idx_product_id", columnList = "product_id"),
        @Index(name = "idx_status", columnList = "status")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Batch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String lotNumber;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private LocalDateTime expirationDate;

    @Column(nullable = false)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BatchStatusEnum status = BatchStatusEnum.ACTIVE;

    @CreationTimestamp
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @OneToMany(mappedBy = "batch", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Transaction> transactions;

    @Version
    private Long version;

    /**
     * Partial update from request - only updates non-null fields
     */
    public void updateFromRequest(BatchRequest request) {
        if (request.getLotNumber() != null && !request.getLotNumber().trim().isEmpty()) {
            this.lotNumber = request.getLotNumber().trim();
        }
        if (request.getQuantity() != null && request.getQuantity() > 0) {
            this.quantity = request.getQuantity();
        }
        if (request.getExpirationDate() != null) {
            this.expirationDate = request.getExpirationDate();
        }
        if (request.getLocation() != null && !request.getLocation().trim().isEmpty()) {
            this.location = request.getLocation().trim();
        }
        if (request.getStatus() != null) {
            this.status = request.getStatus();
        }
    }

    /**
     * Calculate days until expiration
     */
    public long getDaysUntilExpiration() {
        return java.time.temporal.ChronoUnit.DAYS.between(
                java.time.LocalDateTime.now(),
                this.expirationDate
        );
    }

    /**
     * Check if batch is expired
     */
    public boolean isExpired() {
        return getDaysUntilExpiration() < 0;
    }

    /**
     * Get expiration alert level: CRITICAL (< 7), ATTENTION (7-30), NORMAL (> 30)
     */
    public String getExpirationAlertLevel() {
        long daysUntilExpiration = getDaysUntilExpiration();
        if (daysUntilExpiration < 0) return "EXPIRED";
        if (daysUntilExpiration < 7) return "CRITICAL";
        if (daysUntilExpiration <= 30) return "ATTENTION";
        return "NORMAL";
    }
}
