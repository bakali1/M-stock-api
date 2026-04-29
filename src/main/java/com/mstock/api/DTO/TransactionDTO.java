package com.mstock.api.DTO;

import com.mstock.api.Enum.TransactionTypeEnum;
import com.mstock.api.entities.Transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * TransactionDTO - Data Transfer Object for Transaction entity
 * Includes nested user and batch information for display
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionDTO {
    private Long id;
    private TransactionTypeEnum type;
    private Integer quantity;
    private String reason;
    private Instant createdAt;
    private Long userId;
    private String userName;
    private Long productId;
    private String productName;
    private Long batchId;
    private String lotNumber;

    /**
     * Constructor for easy Entity-to-DTO conversion
     */
    public TransactionDTO(Transaction transaction) {
        this.id = transaction.getId();
        this.type = transaction.getType();
        this.quantity = transaction.getQuantity();
        this.reason = transaction.getReason();
        this.createdAt = transaction.getCreatedAt();
        this.userId = transaction.getUser().getId();
        this.userName = transaction.getUser().getUsername();
        this.productId = transaction.getProduct().getId();
        this.productName = transaction.getProduct().getName();
        this.batchId = transaction.getBatch().getId();
        this.lotNumber = transaction.getBatch().getLotNumber();
    }
}
