package com.mstock.api.DTO;

import com.mstock.api.Enum.BatchStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * BatchDTO - Data Transfer Object for Batch entity
 * Excludes relationships (transactions) for API performance
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BatchDTO {
    private Long id;
    private String lotNumber;
    private Integer quantity;
    private LocalDateTime expirationDate;
    private String location;
    private BatchStatusEnum status;
    private Long productId;
    private String productName;
    private String nsnCode;
    private Long daysUntilExpiration;
    private String expirationAlertLevel;

    /**
     * Constructor for easy Entity-to-DTO conversion
     */
    public BatchDTO(com.mstock.api.entities.Batch batch) {
        this.id = batch.getId();
        this.lotNumber = batch.getLotNumber();
        this.quantity = batch.getQuantity();
        this.expirationDate = batch.getExpirationDate();
        this.location = batch.getLocation();
        this.status = batch.getStatus();
        this.productId = batch.getProduct().getId();
        this.productName = batch.getProduct().getName();
        this.nsnCode = batch.getProduct().getNsnCode();
        this.daysUntilExpiration = batch.getDaysUntilExpiration();
        this.expirationAlertLevel = batch.getExpirationAlertLevel();
    }
}
