package com.mstock.api.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mstock.api.DTO.AuditLogDTO;
import com.mstock.api.payload.Request.AuditLogFilter;
import com.mstock.api.payload.Responde.GeneralResponde;
import com.mstock.api.services.AuditLogService;

import lombok.RequiredArgsConstructor;

/**
 * AuditLogController - REST endpoints for audit log retrieval
 * Provides read-only access to immutable audit trail
 * Note: No create/update/delete operations - logs are append-only
 */
@RestController
@RequestMapping("/api/v0/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<GeneralResponde<?>> getAllAuditLogs(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        GeneralResponde<Page<AuditLogDTO>> response = auditLogService.getAllAuditLogs(page, size);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GeneralResponde<?>> getAuditLog(@PathVariable Long id) {
        GeneralResponde<?> response = auditLogService.getAuditLog(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<GeneralResponde<?>> getAuditLogsByUser(@PathVariable Long userId) {
        GeneralResponde<?> response = auditLogService.getAuditLogsByUser(userId);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/table/{tableName}")
    public ResponseEntity<GeneralResponde<?>> getAuditLogsByTable(@PathVariable String tableName) {
        GeneralResponde<?> response = auditLogService.getAuditLogsByTable(tableName);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/search")
    public ResponseEntity<GeneralResponde<Page<AuditLogDTO>>> searchAuditLogs(@RequestBody AuditLogFilter filter) {
        GeneralResponde<Page<AuditLogDTO>> response = auditLogService.searchAuditLogs(filter);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/report")
    public ResponseEntity<GeneralResponde<?>> getChainOfCustodyReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        GeneralResponde<?> response = auditLogService.getChainOfCustodyReport(startDate, endDate);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/statistics")
    public ResponseEntity<GeneralResponde<?>> getAuditStatistics() {
        GeneralResponde<?> response = auditLogService.getAuditStatistics();
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
