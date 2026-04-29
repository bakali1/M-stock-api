package com.mstock.api.payload.Request;

import com.mstock.api.Enum.TransactionTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * TransactionRequest - Request payload for Transaction creation
 * Transactions are immutable after creation, so no update request needed
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionRequest {
    private Long id;  // Required for reads/filters, null for creates
    private TransactionTypeEnum type;  // REQUIRED: IN|OUT|RETURN
    private Integer quantity;  // REQUIRED: must be positive
    private String reason;  // Optional: ex. "Patient Use", "Expired", "Recall"
    private Long batchId;  // REQUIRED: link to batch
    private Long userId;  // Optional: auto-captured from header if not provided
}
