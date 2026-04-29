package com.mstock.api.Mappers;

import com.mstock.api.DTO.AuditLogDTO;
import com.mstock.api.entities.AuditLog;
import org.mapstruct.*;

import java.util.List;

/**
 * AuditLogMapper - MapStruct mapper for AuditLog entity and DTOs
 * Handles Entity ↔ DTO conversions with null-safety
 * Note: AuditLogs are immutable and read-only
 */
@Mapper(componentModel = "spring")
public interface AuditLogMapper {

    /**
     * Convert AuditLog entity to AuditLogDTO
     */
    @BeanMapping(resultType = AuditLogDTO.class, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "userId", source = "auditLog.user.id")
    @Mapping(target = "userName", source = "auditLog.user.username")
    AuditLogDTO toAuditLogDTO(AuditLog auditLog);

    /**
     * Convert list of AuditLog entities to list of AuditLogDTOs
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "userId", source = "auditLog.user.id")
    @Mapping(target = "userName", source = "auditLog.user.username")
    List<AuditLogDTO> toAuditLogDTOList(List<AuditLog> auditLogs);
}
