package com.mstock.api.services;

import com.mstock.api.DTO.AuditLogDTO;
import com.mstock.api.payload.Responde.GeneralResponde;
import com.mstock.api.payload.Request.AuditLogFilter;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * AuditLogService Interface
 * Defines all operations for audit log retrieval and reporting
 * Audit logs are immutable and append-only (read-only operations only)
 */
public interface AuditLogService {

    /**
     * Get all audit logs with pagination
     */
    GeneralResponde<Page<AuditLogDTO>> getAllAuditLogs(Integer page, Integer size);

    /**
     * Get specific audit log by ID
     */
    GeneralResponde<?> getAuditLog(Long id);

    /**
     * Search audit logs with complex filter criteria
     */
    GeneralResponde<Page<AuditLogDTO>> searchAuditLogs(AuditLogFilter filter);

    /**
     * Get audit logs for a specific user
     */
    GeneralResponde<List<AuditLogDTO>> getAuditLogsByUser(Long userId);

    /**
     * Get audit logs for a specific table
     */
    GeneralResponde<List<AuditLogDTO>> getAuditLogsByTable(String tableName);

    /**
     * Get chain of custody report (audit trail for compliance)
     */
    GeneralResponde<List<AuditLogDTO>> getChainOfCustodyReport(String startDate, String endDate);

    /**
     * Export audit logs as PDF (optional - for Phase 2)
     */
    // GeneralResponde<byte[]> exportAuditLogsPDF(AuditLogFilter filter);

    /**
     * Get audit statistics
     */
    GeneralResponde<?> getAuditStatistics();
}
