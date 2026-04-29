package com.mstock.api.payload.Request;

import com.mstock.api.Enum.AuditActionEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * AuditLogFilter - Search/filter criteria for audit logs
 * All fields are optional for flexible querying
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuditLogFilter {
    private Instant startDate;  // Optional: filter from this date
    private Instant endDate;    // Optional: filter until this date
    private Long userId;        // Optional: filter by user
    private AuditActionEnum action;  // Optional: filter by action type
    private String tableName;   // Optional: filter by table name
    private String reason;      // Optional: filter by reason
    private Integer pageNumber; // Default: 0 (first page)
    private Integer pageSize;   // Default: 20 items per page
}
