package com.mstock.api.repositories;

import com.mstock.api.Enum.AuditActionEnum;
import com.mstock.api.entities.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * AuditLogRepository - JPA repository for AuditLog entity
 * Provides read-only append-only access to immutable audit trail logs
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Find all audit logs for a specific user
     */
    List<AuditLog> findByUserIdOrderByTimestampDesc(Long userId);

    /**
     * Find all audit logs by action type
     */
    List<AuditLog> findByActionOrderByTimestampDesc(AuditActionEnum action);

    /**
     * Find all audit logs for a specific table
     */
    List<AuditLog> findByTableNameOrderByTimestampDesc(String tableName);

    /**
     * Find audit logs within a date range
     */
    List<AuditLog> findByTimestampBetweenOrderByTimestampDesc(Instant startDate, Instant endDate);

    /**
     * Find audit logs by reason (optional field)
     */
    List<AuditLog> findByReasonOrderByTimestampDesc(String reason);

    /**
     * Complex query: find logs with multiple filter criteria
     */
    @Query("SELECT al FROM AuditLog al WHERE " +
           "(:userId IS NULL OR al.user.id = :userId) AND " +
           "(:action IS NULL OR al.action = :action) AND " +
           "(:tableName IS NULL OR al.tableName = :tableName) AND " +
           "(:startDate IS NULL OR al.timestamp >= :startDate) AND " +
           "(:endDate IS NULL OR al.timestamp <= :endDate) " +
           "ORDER BY al.timestamp DESC")
    Page<AuditLog> findByFilterCriteria(
            @Param("userId") Long userId,
            @Param("action") AuditActionEnum action,
            @Param("tableName") String tableName,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            Pageable pageable
    );

    /**
     * Find all old audit logs (older than specified date) for archival
     */
    @Query("SELECT al FROM AuditLog al WHERE al.timestamp < :cutoffDate ORDER BY al.timestamp ASC")
    List<AuditLog> findOldAuditLogs(@Param("cutoffDate") Instant cutoffDate);

    /**
     * Count audit logs for statistics
     */
    long countByUserId(Long userId);

    /**
     * Count audit logs by action type
     */
    long countByAction(AuditActionEnum action);
}
