package com.mstock.api.DTO;

import com.mstock.api.Enum.AuditActionEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * AuditLogDTO - Data Transfer Object for AuditLog entity
 * Provides read-only audit trail information for API responses
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuditLogDTO {
    private Long id;
    private AuditActionEnum action;
    private String tableName;
    private String oldValue;
    private String newValue;
    private String ipAddress;
    private String reason;
    private Instant timestamp;
    private Long userId;
    private String userName;

    /**
     * Constructor for easy Entity-to-DTO conversion
     */
    public AuditLogDTO(com.mstock.api.entities.AuditLog auditLog) {
        this.id = auditLog.getId();
        this.action = auditLog.getAction();
        this.tableName = auditLog.getTableName();
        this.oldValue = auditLog.getOldValue();
        this.newValue = auditLog.getNewValue();
        this.ipAddress = auditLog.getIpAddress();
        this.reason = auditLog.getReason();
        this.timestamp = auditLog.getTimestamp();
        if (auditLog.getUser() != null) {
            this.userId = auditLog.getUser().getId();
            this.userName = auditLog.getUser().getUsername();
        }
    }
}
