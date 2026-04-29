package com.mstock.api.services.imp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mstock.api.DTO.AuditLogDTO;
import com.mstock.api.Enum.AuditActionEnum;
import com.mstock.api.entities.AuditLog;
import com.mstock.api.payload.Request.AuditLogFilter;
import com.mstock.api.payload.Responde.GeneralResponde;
import com.mstock.api.repositories.AuditLogRepository;
import com.mstock.api.services.AuditLogService;

import lombok.RequiredArgsConstructor;

/**
 * AuditLogServiceImpl - Implementation of AuditLogService
 * Provides read-only access to immutable audit trail logs
 */
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    private static final int DEFAULT_PAGE_SIZE = 20;

    @Override
    @Transactional(readOnly = true)
    public GeneralResponde<Page<AuditLogDTO>> getAllAuditLogs(Integer page, Integer size) {
        if (page == null) page = 0;
        if (size == null) size = DEFAULT_PAGE_SIZE;

        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> auditLogs = auditLogRepository.findAll(pageable);
        Page<AuditLogDTO> auditLogDTOs = auditLogs.map(AuditLogDTO::new);

        return GeneralResponde.<Page<AuditLogDTO>>builder()
                .status(HttpStatus.OK.value())
                .msg("All audit logs (page " + (page + 1) + " of " + auditLogs.getTotalPages() + ")")
                .data(auditLogDTOs)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GeneralResponde<?> getAuditLog(Long id) {
        AuditLog auditLog = auditLogRepository.findById(id).orElse(null);
        if (auditLog == null) {
            return GeneralResponde.builder()
                    .status(HttpStatus.NOT_FOUND.value())
                    .msg("Audit log not found")
                    .build();
        }

        AuditLogDTO auditLogDTO = new AuditLogDTO(auditLog);
        return GeneralResponde.builder()
                .status(HttpStatus.OK.value())
                .msg("Audit log found")
                .data(auditLogDTO)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GeneralResponde<Page<AuditLogDTO>> searchAuditLogs(AuditLogFilter filter) {
        if (filter == null) {
            filter = new AuditLogFilter();
        }

        if (filter.getPageNumber() == null) {
            filter.setPageNumber(0);
        }
        if (filter.getPageSize() == null) {
            filter.setPageSize(DEFAULT_PAGE_SIZE);
        }

        Pageable pageable = PageRequest.of(filter.getPageNumber(), filter.getPageSize());

        Page<AuditLog> auditLogs = auditLogRepository.findByFilterCriteria(
                filter.getUserId(),
                filter.getAction(),
                filter.getTableName(),
                filter.getStartDate(),
                filter.getEndDate(),
                pageable
        );

        Page<AuditLogDTO> auditLogDTOs = auditLogs.map(AuditLogDTO::new);

        return GeneralResponde.<Page<AuditLogDTO>>builder()
                .status(HttpStatus.OK.value())
                .msg("Audit logs matching filter criteria")
                .data(auditLogDTOs)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GeneralResponde<List<AuditLogDTO>> getAuditLogsByUser(Long userId) {
        List<AuditLog> auditLogs = auditLogRepository.findByUserIdOrderByTimestampDesc(userId);
        List<AuditLogDTO> auditLogDTOs = auditLogs.stream()
                .map(AuditLogDTO::new)
                .collect(Collectors.toList());

        return GeneralResponde.<List<AuditLogDTO>>builder()
                .status(HttpStatus.OK.value())
                .msg("Audit logs for user " + userId)
                .data(auditLogDTOs)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GeneralResponde<List<AuditLogDTO>> getAuditLogsByTable(String tableName) {
        List<AuditLog> auditLogs = auditLogRepository.findByTableNameOrderByTimestampDesc(tableName);
        List<AuditLogDTO> auditLogDTOs = auditLogs.stream()
                .map(AuditLogDTO::new)
                .collect(Collectors.toList());

        return GeneralResponde.<List<AuditLogDTO>>builder()
                .status(HttpStatus.OK.value())
                .msg("Audit logs for table " + tableName)
                .data(auditLogDTOs)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GeneralResponde<List<AuditLogDTO>> getChainOfCustodyReport(String startDateStr, String endDateStr) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            Instant startDate = null;
            Instant endDate = null;

            if (startDateStr != null && !startDateStr.isEmpty()) {
                LocalDateTime localStart = LocalDateTime.parse(startDateStr, formatter);
                startDate = localStart.atZone(ZoneId.systemDefault()).toInstant();
            }

            if (endDateStr != null && !endDateStr.isEmpty()) {
                LocalDateTime localEnd = LocalDateTime.parse(endDateStr, formatter);
                endDate = localEnd.atZone(ZoneId.systemDefault()).toInstant();
            }

            List<AuditLog> auditLogs;
            if (startDate != null && endDate != null) {
                auditLogs = auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(startDate, endDate);
            } else {
                auditLogs = auditLogRepository.findAll();
            }

            List<AuditLogDTO> auditLogDTOs = auditLogs.stream()
                    .map(AuditLogDTO::new)
                    .collect(Collectors.toList());

            return GeneralResponde.<List<AuditLogDTO>>builder()
                    .status(HttpStatus.OK.value())
                    .msg("Chain of Custody Report")
                    .data(auditLogDTOs)
                    .build();
        } catch (Exception e) {
            return GeneralResponde.<List<AuditLogDTO>>builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .msg("Invalid date format. Use ISO format: yyyy-MM-ddTHH:mm:ss")
                    .build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public GeneralResponde<?> getAuditStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // Count by action type
        stats.put("creates", auditLogRepository.countByAction(AuditActionEnum.CREATE));
        stats.put("updates", auditLogRepository.countByAction(AuditActionEnum.UPDATE));
        stats.put("deletes", auditLogRepository.countByAction(AuditActionEnum.DELETE));

        // Total audit logs
        stats.put("total", auditLogRepository.count());

        return GeneralResponde.builder()
                .status(HttpStatus.OK.value())
                .msg("Audit Statistics")
                .data(stats)
                .build();
    }
}
