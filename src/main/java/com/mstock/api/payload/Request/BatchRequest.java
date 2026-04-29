package com.mstock.api.payload.Request;

import com.mstock.api.Enum.BatchStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * BatchRequest - Request payload for Batch creation/update
 * Used for API input validation
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BatchRequest {
    private Long id;  // Required for updates, null for creates
    private String lotNumber;
    private Integer quantity;
    private LocalDateTime expirationDate;
    private String location;
    private BatchStatusEnum status;
    private Long productId;  // Required to link to Product
}
